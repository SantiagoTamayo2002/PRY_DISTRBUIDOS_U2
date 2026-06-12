package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    private String symbol;
    private String name;
    private double price;
    private int availableShares;
    private int quantity; // Used for userStocks
}
