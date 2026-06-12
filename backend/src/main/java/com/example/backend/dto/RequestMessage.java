package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {
    private int lamportClock;
    private int senderNodeId;
    private String stockSymbol;
    private int quantity;
}
