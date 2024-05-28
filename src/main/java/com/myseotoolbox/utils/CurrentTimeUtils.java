package com.myseotoolbox.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CurrentTimeUtils implements TimeUtils {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
