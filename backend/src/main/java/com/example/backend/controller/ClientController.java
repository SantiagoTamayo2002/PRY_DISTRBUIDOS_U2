package com.example.backend.controller;

import com.example.backend.model.NodeState;
import com.example.backend.service.JsonDatabaseService;
import com.example.backend.service.StockService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*") // Allow frontend to connect
@RequiredArgsConstructor
public class ClientController {

    private final StockService stockService;
    private final JsonDatabaseService dbService;

    @GetMapping("/state")
    public ResponseEntity<NodeState> getState() {
        return ResponseEntity.ok(dbService.readState());
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyStock(@RequestBody BuyRequest request) {
        boolean success = stockService.buyStock(request.getSymbol(), request.getQuantity());
        if (success) {
            return ResponseEntity.ok("Successfully bought stock");
        } else {
            return ResponseEntity.badRequest().body("Failed to buy stock. Insufficient funds or unavailable shares.");
        }
    }

    @Data
    public static class BuyRequest {
        private String symbol;
        private int quantity;
    }
}
