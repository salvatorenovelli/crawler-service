package com.myseotoolbox.crawler;

import org.junit.Test;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ScanSchedulerTest {

    public static final int BASE_TEST_DATE = 10;
    CronSequenceGenerator sut = new CronSequenceGenerator(ScanScheduler.EVERY_DAY_AT_09_PM);

    @Test
    public void upTo9PMisScheduledSameDay() {
        for (int hours = 0; hours < 20; hours++) {
            Date dateTime = testDateForHourOfDay(hours);
            Date next = sut.next(dateTime);
            assertThat(next, is(expectedDateForHourOfDay(21)));
        }
    }

    @Test
    public void after9PMisScheduledNextDay() {
        for (int hours = 21; hours < 23; hours++) {
            Date dateTime = testDateForHourOfDay(hours);
            Date next = sut.next(dateTime);
            assertThat(next, is(expectedDateForHourOfDay(BASE_TEST_DATE + 1, 21)));
        }
    }

    private Date testDateForHourOfDay(int hourofday) {
        return testDateForHourOfDay(BASE_TEST_DATE, hourofday);
    }

    private Date testDateForHourOfDay(int day, int hours) {
        return convertToDate(LocalDateTime.parse("1930-12-" + day + "T" + String.format("%02d", hours) + ":59:59"));
    }

    private Date expectedDateForHourOfDay(int hourofday) {
        return expectedDateForHourOfDay(BASE_TEST_DATE, hourofday);
    }

    private Date expectedDateForHourOfDay(int day, int hours) {
        return convertToDate(LocalDateTime.parse("1930-12-" + day + "T" + String.format("%02d", hours) + ":00:00"));
    }

    private Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}