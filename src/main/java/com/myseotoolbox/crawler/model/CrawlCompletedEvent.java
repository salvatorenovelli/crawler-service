package com.myseotoolbox.crawler.model;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlCompletedEvent {
   private WebsiteCrawl websiteCrawl;
}
