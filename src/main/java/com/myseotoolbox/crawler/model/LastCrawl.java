package com.myseotoolbox.crawler.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LastCrawl {
    private String websiteCrawlId;
    private LocalDateTime dateTime;

    private InboundLinksCount inboundLinksCount;
}
