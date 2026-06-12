package com.example.backend.controller;

import com.example.backend.dto.BullyMessage;
import com.example.backend.dto.MarketUpdateMessage;
import com.example.backend.dto.ReplyMessage;
import com.example.backend.dto.RequestMessage;
import com.example.backend.service.BullyService;
import com.example.backend.service.RicartAgrawalaService;
import com.example.backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distributed")
@RequiredArgsConstructor
public class InterNodeController {

    private final RicartAgrawalaService ricartAgrawalaService;
    private final BullyService bullyService;
    private final StockService stockService;

    @PostMapping("/request")
    public ResponseEntity<Void> receiveRequest(@RequestBody RequestMessage request) {
        ricartAgrawalaService.handleRequest(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply")
    public ResponseEntity<Void> receiveReply(@RequestBody ReplyMessage reply) {
        ricartAgrawalaService.handleReply(reply);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/election")
    public ResponseEntity<Void> receiveElectionMessage(@RequestBody BullyMessage message) {
        bullyService.handleMessage(message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-market")
    public ResponseEntity<Void> receiveMarketUpdate(@RequestBody MarketUpdateMessage message) {
        stockService.applyMarketUpdate(message);
        return ResponseEntity.ok().build();
    }
}
