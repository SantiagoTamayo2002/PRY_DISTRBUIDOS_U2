package com.example.backend.service;

import com.example.backend.config.NodeConfig;
import com.example.backend.dto.MarketUpdateMessage;
import com.example.backend.model.NodeState;
import com.example.backend.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final JsonDatabaseService dbService;
    private final RicartAgrawalaService ricartAgrawalaService;
    private final LamportClockService lamportClockService;
    private final NodeConfig nodeConfig;
    private final RestTemplate restTemplate;
    private final WebSocketMessageService wsMessageService;

    public boolean buyStock(String symbol, int quantity) {
        log.info("Attempting to buy {} shares of {}", quantity, symbol);
        
        try {
            // 1. Enter Critical Section
            ricartAgrawalaService.requestCriticalSection(symbol, quantity);
            
            // 2. Perform Transaction
            NodeState state = dbService.readState();
            Optional<Stock> stockOpt = state.getMarketStocks().stream()
                    .filter(s -> s.getSymbol().equals(symbol))
                    .findFirst();

            if (stockOpt.isPresent()) {
                Stock marketStock = stockOpt.get();
                if (marketStock.getAvailableShares() >= quantity) {
                    double totalCost = marketStock.getPrice() * quantity;
                    if (state.getAccountBalance() >= totalCost) {
                        // Deduct balance
                        state.setAccountBalance(state.getAccountBalance() - totalCost);
                        // Deduct available shares
                        marketStock.setAvailableShares(marketStock.getAvailableShares() - quantity);
                        
                        // Add to user stocks
                        Optional<Stock> userStockOpt = state.getUserStocks().stream()
                                .filter(s -> s.getSymbol().equals(symbol))
                                .findFirst();
                                
                        if (userStockOpt.isPresent()) {
                            Stock userStock = userStockOpt.get();
                            userStock.setQuantity(userStock.getQuantity() + quantity);
                        } else {
                            Stock newStock = new Stock();
                            newStock.setSymbol(symbol);
                            newStock.setName(marketStock.getName());
                            newStock.setPrice(marketStock.getPrice());
                            newStock.setQuantity(quantity);
                            state.getUserStocks().add(newStock);
                        }
                        
                        // 3. Persist State
                        dbService.writeState(state);
                        wsMessageService.broadcastStateUpdate(state);
                        wsMessageService.broadcastLog("Successfully bought " + quantity + " " + symbol, nodeConfig.getMyNodeId());

                        // 4. Broadcast Market Update
                        broadcastMarketUpdate(state);
                        return true;
                    } else {
                        wsMessageService.broadcastLog("Insufficient funds for " + symbol, nodeConfig.getMyNodeId());
                    }
                } else {
                    wsMessageService.broadcastLog("Not enough available shares for " + symbol, nodeConfig.getMyNodeId());
                }
            } else {
                wsMessageService.broadcastLog("Stock " + symbol + " not found", nodeConfig.getMyNodeId());
            }
            return false;
        } finally {
            // 5. Release Critical Section
            ricartAgrawalaService.releaseCriticalSection();
        }
    }

    private void broadcastMarketUpdate(NodeState state) {
        int clock = lamportClockService.increment();
        MarketUpdateMessage msg = new MarketUpdateMessage(clock, nodeConfig.getMyNodeId(), state.getMarketStocks());
        
        for (Map.Entry<Integer, String> entry : nodeConfig.getOtherNodes().entrySet()) {
            try {
                restTemplate.postForEntity(entry.getValue() + "/api/distributed/update-market", msg, Void.class);
            } catch (Exception e) {
                log.warn("Failed to send market update to node {}", entry.getKey());
            }
        }
    }

    public void applyMarketUpdate(MarketUpdateMessage message) {
        lamportClockService.update(message.getLamportClock());
        wsMessageService.broadcastLamportClock(lamportClockService.getClock(), nodeConfig.getMyNodeId());
        
        // This method can be called outside of critical section because it just updates market inventory.
        // But to avoid overriding local writes, we use the lock inside dbService.
        NodeState state = dbService.readState();
        state.setMarketStocks(message.getMarketStocks());
        dbService.writeState(state);
        wsMessageService.broadcastStateUpdate(state);
        wsMessageService.broadcastLog("Applied Market Update from node " + message.getSenderNodeId(), nodeConfig.getMyNodeId());
    }
}
