package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeState {
    private int nodeId;
    private double accountBalance;
    private List<Stock> userStocks = new ArrayList<>();
    private List<Stock> marketStocks = new ArrayList<>();
}
