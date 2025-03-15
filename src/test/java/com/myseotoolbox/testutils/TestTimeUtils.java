package com.myseotoolbox.testutils;

import com.myseotoolbox.utils.TimeUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TestTimeUtils implements TimeUtils {

    private Instant curTime = Instant.EPOCH;

    public void addDays(int days) {
        curTime = curTime.plus(days, ChronoUnit.DAYS);
    }

    @Override
    public Instant now() {
        return curTime;
    }
}
