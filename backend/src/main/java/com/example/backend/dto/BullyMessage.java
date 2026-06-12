package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BullyMessage {
    private String type; // ELECTION, ANSWER, COORDINATOR, HEARTBEAT
    private int senderNodeId;
}
