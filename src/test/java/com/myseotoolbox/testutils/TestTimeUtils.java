package com.myseotoolbox.testutils;

import com.myseotoolbox.utils.TimeUtils;

import java.time.Instant;

public class TestTimeUtils implements TimeUtils {

    private final Instant curTime = Instant.EPOCH;


    @Override
    public Instant now() {
        return curTime;
    }
}
