package com.myseotoolbox.crawler.testutils;


import com.myseotoolbox.crawler.CalendarService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TestCalendarService extends CalendarService {


    public static final Date DEFAULT_TEST_DAY = testDay(0);

    @Override
    public Date now() {
        return DEFAULT_TEST_DAY;
    }

    @Override
    public LocalDate today() {
        return toLocalDate(now());
    }

    public static Date testDay(int offset) {
        return localDateTimeToDate(LocalDateTime.parse("2000-01-01T00:00:00").plusDays(offset));
    }

    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date localDateToDate(LocalDate dateTime) {
        return Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
