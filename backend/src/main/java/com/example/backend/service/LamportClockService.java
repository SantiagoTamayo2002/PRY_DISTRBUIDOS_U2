package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LamportClockService {

    private final AtomicInteger clock = new AtomicInteger(0);

    public int getClock() {
        return clock.get();
    }

    /**
     * Local event: increments the clock by 1.
     */
    public int increment() {
        int newClock = clock.incrementAndGet();
        log.debug("Lamport clock incremented locally to: {}", newClock);
        return newClock;
    }

    /**
     * Message receive event: max(local, message_clock) + 1
     */
    public int update(int receivedClock) {
        return clock.updateAndGet(current -> Math.max(current, receivedClock) + 1);
    }
}
