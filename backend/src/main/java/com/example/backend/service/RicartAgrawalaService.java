package com.example.backend.service;

import com.example.backend.config.NodeConfig;
import com.example.backend.dto.ReplyMessage;
import com.example.backend.dto.RequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RicartAgrawalaService {

    public enum State {
        RELEASED, WANTED, HELD
    }

    private State state = State.RELEASED;
    private int myRequestTimestamp = 0;
    
    // Threads waiting for critical section
    private final Object criticalSectionMonitor = new Object();
    
    // Replies received
    private final AtomicInteger repliesReceived = new AtomicInteger(0);
    
    // Deferred requests from other nodes
    private final Queue<RequestMessage> deferredRequests = new ConcurrentLinkedQueue<>();

    private final NodeConfig nodeConfig;
    private final LamportClockService lamportClockService;
    private final RestTemplate restTemplate;
    private final WebSocketMessageService wsMessageService;

    public void requestCriticalSection(String stockSymbol, int quantity) {
        log.info("Requesting Critical Section for {} shares of {}", quantity, stockSymbol);
        wsMessageService.broadcastLog("Requesting Critical Section (WANTED)", nodeConfig.getMyNodeId());
        
        synchronized (this) {
            state = State.WANTED;
            myRequestTimestamp = lamportClockService.increment();
            repliesReceived.set(0);
        }

        Map<Integer, String> otherNodes = nodeConfig.getOtherNodes();
        if (otherNodes.isEmpty()) {
            // We are the only node
            synchronized (this) {
                state = State.HELD;
            }
            wsMessageService.broadcastLog("Entered Critical Section (HELD) - No other nodes", nodeConfig.getMyNodeId());
            return;
        }

        RequestMessage reqMsg = new RequestMessage(myRequestTimestamp, nodeConfig.getMyNodeId(), stockSymbol, quantity);
        
        // Broadcast REQUEST to all other nodes
        for (Map.Entry<Integer, String> entry : otherNodes.entrySet()) {
            String targetUrl = entry.getValue() + "/api/distributed/request";
            try {
                restTemplate.postForEntity(targetUrl, reqMsg, Void.class);
            } catch (Exception e) {
                log.warn("Failed to send REQUEST to node {}: {}", entry.getKey(), e.getMessage());
                // Assume node is down, count as reply to not deadlock
                repliesReceived.incrementAndGet();
            }
        }

        // Wait until N-1 replies are received
        synchronized (criticalSectionMonitor) {
            while (repliesReceived.get() < otherNodes.size()) {
                try {
                    criticalSectionMonitor.wait(100);
                    // Add timeout/recheck logic if nodes go down?
                    // For simplicity, we just check count
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        synchronized (this) {
            state = State.HELD;
        }
        wsMessageService.broadcastLog("Entered Critical Section (HELD)", nodeConfig.getMyNodeId());
    }

    public synchronized void releaseCriticalSection() {
        log.info("Releasing Critical Section");
        wsMessageService.broadcastLog("Releasing Critical Section (RELEASED)", nodeConfig.getMyNodeId());
        state = State.RELEASED;
        
        lamportClockService.increment(); // increment clock on release event? Or let the next message do it.

        // Send REPLY to all deferred requests
        while (!deferredRequests.isEmpty()) {
            RequestMessage req = deferredRequests.poll();
            ReplyMessage replyMsg = new ReplyMessage(lamportClockService.getClock(), nodeConfig.getMyNodeId());
            String targetUrl = nodeConfig.getAllNodes().get(req.getSenderNodeId()) + "/api/distributed/reply";
            try {
                restTemplate.postForEntity(targetUrl, replyMsg, Void.class);
            } catch (Exception e) {
                log.warn("Failed to send deferred REPLY to node {}: {}", req.getSenderNodeId(), e.getMessage());
            }
        }
    }

    public synchronized void handleRequest(RequestMessage request) {
        lamportClockService.update(request.getLamportClock());
        wsMessageService.broadcastLamportClock(lamportClockService.getClock(), nodeConfig.getMyNodeId());
        
        boolean defer = false;
        
        if (state == State.HELD) {
            defer = true;
        } else if (state == State.WANTED) {
            // Compare timestamps:
            // if request.lamportClock < myRequestTimestamp -> they have priority, don't defer
            // if request.lamportClock > myRequestTimestamp -> I have priority, defer
            // if equal -> use node ID to break tie
            if (request.getLamportClock() > myRequestTimestamp) {
                defer = true;
            } else if (request.getLamportClock() == myRequestTimestamp) {
                if (request.getSenderNodeId() > nodeConfig.getMyNodeId()) {
                    defer = true;
                }
            }
        }
        
        if (defer) {
            wsMessageService.broadcastLog("Deferring request from Node " + request.getSenderNodeId(), nodeConfig.getMyNodeId());
            deferredRequests.add(request);
        } else {
            // Send reply immediately
            ReplyMessage replyMsg = new ReplyMessage(lamportClockService.increment(), nodeConfig.getMyNodeId());
            String targetUrl = nodeConfig.getAllNodes().get(request.getSenderNodeId()) + "/api/distributed/reply";
            try {
                restTemplate.postForEntity(targetUrl, replyMsg, Void.class);
            } catch (Exception e) {
                log.warn("Failed to send REPLY to node {}: {}", request.getSenderNodeId(), e.getMessage());
            }
        }
    }

    public void handleReply(ReplyMessage reply) {
        lamportClockService.update(reply.getLamportClock());
        wsMessageService.broadcastLamportClock(lamportClockService.getClock(), nodeConfig.getMyNodeId());
        
        int currentReplies = repliesReceived.incrementAndGet();
        Map<Integer, String> otherNodes = nodeConfig.getOtherNodes();
        
        if (currentReplies >= otherNodes.size()) {
            synchronized (criticalSectionMonitor) {
                criticalSectionMonitor.notifyAll();
            }
        }
    }
}
