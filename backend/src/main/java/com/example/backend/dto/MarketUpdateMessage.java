package com.example.backend.dto;

import com.example.backend.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketUpdateMessage {
    private int lamportClock;
    private int senderNodeId;
    private List<Stock> marketStocks;
}
