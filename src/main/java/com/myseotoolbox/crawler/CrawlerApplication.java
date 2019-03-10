package com.myseotoolbox.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class CrawlerApplication {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in thread " + t, e));
        SpringApplication.run(CrawlerApplication.class, args);
    }
}


