package com.myseotoolbox.crawler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlWorkspaceRequest {
    private int workspaceNumber;
    private int numConnections;
    private String crawlOwner;
}
