package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoredUri {

    @Id
    private String id;
    @Indexed
    private String uri;

    private String ownerName;

    @Indexed int workspaceNumber;

    private Recommendation recommendation;

    private PageSnapshot currentValue;

    private LastCrawl lastCrawl;

    private String status;

}

