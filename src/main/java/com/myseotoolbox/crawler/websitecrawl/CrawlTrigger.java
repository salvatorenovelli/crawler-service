package com.myseotoolbox.crawler.websitecrawl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@RequiredArgsConstructor
public abstract class CrawlTrigger {
    public enum Type {
        SCHEDULED,
        USER_INITIATED_WORKSPACE
    }

    private final Type type;
}