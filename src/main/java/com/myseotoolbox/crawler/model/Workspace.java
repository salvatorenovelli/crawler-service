package com.myseotoolbox.crawler.model;


import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Set;

@NoArgsConstructor
@Data
public class Workspace {
    @Id private String id;
    private Integer seqNumber;
    private String name;
    private String ownerName;
    private Set<String> users;

    private String countryCode;
    private String websiteUrl;
    private CrawlerSettings crawlerSettings;
}

