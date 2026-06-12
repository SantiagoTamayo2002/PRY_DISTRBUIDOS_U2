package com.example.backend.service;

import com.example.backend.config.NodeConfig;
import com.example.backend.dto.BullyMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class BullyService {

    private final NodeConfig nodeConfig;
    private final RestTemplate restTemplate;
    private final WebSocketMessageService wsMessageService;

    private int coordinatorId = -1;
    private final AtomicBoolean electionInProgress = new AtomicBoolean(false);
    private long lastHeartbeatTime = System.currentTimeMillis();
    private static final long HEARTBEAT_TIMEOUT = 5000; // 5 seconds
    private static final long ELECTION_TIMEOUT = 3000;

    @PostConstruct
    public void init() {
        // Assume highest node is coordinator initially or start election
        startElection();
    }

    @Scheduled(fixedRate = 2000)
    public void sendHeartbeat() {
        if (coordinatorId == nodeConfig.getMyNodeId()) {
            // I am coordinator, send heartbeat to others
            BullyMessage msg = new BullyMessage("HEARTBEAT", nodeConfig.getMyNodeId());
            for (Map.Entry<Integer, String> entry : nodeConfig.getOtherNodes().entrySet()) {
                try {
                    restTemplate.postForEntity(entry.getValue() + "/api/distributed/election", msg, Void.class);
                } catch (Exception e) {
                    // Ignore, node might be down
                }
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void checkCoordinatorHealth() {
        if (coordinatorId != nodeConfig.getMyNodeId() && coordinatorId != -1) {
            if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT) {
                if (!electionInProgress.get()) {
                    log.warn("Coordinator {} is presumed dead. Starting election.", coordinatorId);
                    wsMessageService.broadcastLog("Coordinator dead, starting election", nodeConfig.getMyNodeId());
                    startElection();
                }
            }
        }
    }

    public void startElection() {
        electionInProgress.set(true);
        boolean higherNodeResponded = false;
        BullyMessage electionMsg = new BullyMessage("ELECTION", nodeConfig.getMyNodeId());

        for (Map.Entry<Integer, String> entry : nodeConfig.getOtherNodes().entrySet()) {
            if (entry.getKey() > nodeConfig.getMyNodeId()) {
                try {
                    restTemplate.postForEntity(entry.getValue() + "/api/distributed/election", electionMsg, Void.class);
                    higherNodeResponded = true;
                } catch (Exception e) {
                    log.debug("Node {} did not respond to ELECTION", entry.getKey());
                }
            }
        }

        if (!higherNodeResponded) {
            // Wait a bit to see if any answer comes in concurrently? In basic Bully, if no answer, we are coordinator.
            proclaimCoordinator();
        }
        // If a higher node responded, we just wait for the COORDINATOR message.
    }

    public void proclaimCoordinator() {
        coordinatorId = nodeConfig.getMyNodeId();
        electionInProgress.set(false);
        log.info("I am the new Coordinator!");
        wsMessageService.broadcastLeader(coordinatorId);
        wsMessageService.broadcastLog("I am the new Coordinator!", nodeConfig.getMyNodeId());

        BullyMessage coordMsg = new BullyMessage("COORDINATOR", nodeConfig.getMyNodeId());
        for (Map.Entry<Integer, String> entry : nodeConfig.getOtherNodes().entrySet()) {
            try {
                restTemplate.postForEntity(entry.getValue() + "/api/distributed/election", coordMsg, Void.class);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void handleMessage(BullyMessage message) {
        String type = message.getType();
        int senderId = message.getSenderNodeId();

        switch (type) {
            case "ELECTION":
                // A lower node called an election. We must answer and start our own election if not already.
                BullyMessage answerMsg = new BullyMessage("ANSWER", nodeConfig.getMyNodeId());
                try {
                    restTemplate.postForEntity(nodeConfig.getAllNodes().get(senderId) + "/api/distributed/election", answerMsg, Void.class);
                } catch (Exception e) {
                    // Ignore
                }
                if (!electionInProgress.get()) {
                    startElection();
                }
                break;
            case "ANSWER":
                // We received an answer from a higher node. We lose election.
                electionInProgress.set(true);
                // Just wait for COORDINATOR message.
                break;
            case "COORDINATOR":
                coordinatorId = senderId;
                electionInProgress.set(false);
                lastHeartbeatTime = System.currentTimeMillis();
                wsMessageService.broadcastLeader(coordinatorId);
                log.info("New Coordinator is {}", coordinatorId);
                break;
            case "HEARTBEAT":
                if (senderId >= coordinatorId) { // Only accept heartbeats from supposed coordinator or higher
                    coordinatorId = senderId;
                    lastHeartbeatTime = System.currentTimeMillis();
                }
                break;
        }
    }
}
