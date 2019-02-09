package com.myseotoolbox.crawler;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


@Component
public class CalendarService {
    public Date now() {
        return new Date();
    }

    public LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}


