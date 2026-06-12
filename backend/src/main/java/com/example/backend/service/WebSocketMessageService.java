package com.example.backend.service;

import com.example.backend.model.NodeState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastLamportClock(int clock, int nodeId) {
        messagingTemplate.convertAndSend("/topic/lamport", new LamportUpdate(clock, nodeId));
    }

    public void broadcastStateUpdate(NodeState state) {
        messagingTemplate.convertAndSend("/topic/state", state);
    }

    public void broadcastLeader(int leaderNodeId) {
        messagingTemplate.convertAndSend("/topic/leader", new LeaderUpdate(leaderNodeId));
    }
    
    public void broadcastLog(String message, int nodeId) {
        messagingTemplate.convertAndSend("/topic/logs", new LogUpdate(message, nodeId));
    }

    @Data
    @AllArgsConstructor
    public static class LamportUpdate {
        private int clock;
        private int nodeId;
    }

    @Data
    @AllArgsConstructor
    public static class LeaderUpdate {
        private int leaderNodeId;
    }
    
    @Data
    @AllArgsConstructor
    public static class LogUpdate {
        private String message;
        private int nodeId;
    }
}
