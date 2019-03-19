package com.myseotoolbox.crawler.spider.model;


import lombok.Data;

import java.time.LocalDate;

@Data
public class WebsiteCrawlLog {
    private final String origin;
    private final LocalDate date;
}
