package com.example.backend.service;

import com.example.backend.model.NodeState;
import com.example.backend.model.Stock;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class JsonDatabaseService {

    @Value("${node.id:1}")
    private int nodeId;

    private String getFilePath() {
        return "node" + nodeId + "_db.json";
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        lock.lock();
        try {
            File dbFile = new File(getFilePath());
            if (!dbFile.exists()) {
                log.info("Creating initial database file: {}", getFilePath());
                NodeState initialState = new NodeState();
                initialState.setNodeId(nodeId);
                initialState.setAccountBalance(10000.0);
                
                Stock initialStock = new Stock();
                initialStock.setSymbol("STR");
                initialStock.setName("Star Stocks");
                initialStock.setPrice(150.0);
                initialStock.setAvailableShares(100);
                initialStock.setQuantity(0);

                initialState.getMarketStocks().add(initialStock);
                
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, initialState);
            }
        } catch (IOException e) {
            log.error("Failed to initialize database file", e);
        } finally {
            lock.unlock();
        }
    }

    public NodeState readState() {
        lock.lock();
        try {
            File dbFile = new File(getFilePath());
            if (dbFile.exists()) {
                return objectMapper.readValue(dbFile, NodeState.class);
            }
            return new NodeState();
        } catch (IOException e) {
            log.error("Failed to read database file", e);
            throw new RuntimeException("Failed to read database", e);
        } finally {
            lock.unlock();
        }
    }

    public void writeState(NodeState state) {
        lock.lock();
        try {
            File dbFile = new File(getFilePath());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, state);
            log.info("Successfully updated state for node {}", nodeId);
        } catch (IOException e) {
            log.error("Failed to write to database file", e);
            throw new RuntimeException("Failed to write to database", e);
        } finally {
            lock.unlock();
        }
    }
}
