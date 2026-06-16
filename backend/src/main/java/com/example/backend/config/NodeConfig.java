package com.example.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NodeConfig {

    @Value("${node.id:1}")
    @Getter
    private int myNodeId;

    @Value("${nodes.all:1:localhost:8081,2:localhost:8082,3:localhost:8083}")
    private String nodesAllStr;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Returns a map of Node ID to Base URL (e.g., 2 -> "http://192.168.1.1:8082")
     * Excludes the current node.
     * Format: id:host:port (e.g., 1:192.168.1.1:8081,2:192.168.1.2:8081)
     */
    public Map<Integer, String> getOtherNodes() {
        Map<Integer, String> otherNodes = new HashMap<>();
        String[] nodeConfigs = nodesAllStr.split(",");
        for (String config : nodeConfigs) {
            String[] parts = config.split(":");
            int id = Integer.parseInt(parts[0]);
            String host = parts[1];
            String port = parts[2];
            if (id != myNodeId) {
                otherNodes.put(id, "http://" + host + ":" + port);
            }
        }
        return otherNodes;
    }
    
    /**
     * Returns a map of Node ID to Base URL for ALL nodes.
     * Format: id:host:port (e.g., 1:192.168.1.1:8081,2:192.168.1.2:8081)
     */
    public Map<Integer, String> getAllNodes() {
        Map<Integer, String> allNodes = new HashMap<>();
        String[] nodeConfigs = nodesAllStr.split(",");
        for (String config : nodeConfigs) {
            String[] parts = config.split(":");
            int id = Integer.parseInt(parts[0]);
            String host = parts[1];
            String port = parts[2];
            allNodes.put(id, "http://" + host + ":" + port);
        }
        return allNodes;
    }
}
