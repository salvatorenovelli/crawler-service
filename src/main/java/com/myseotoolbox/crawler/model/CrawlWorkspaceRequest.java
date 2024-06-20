package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlWorkspaceRequest {
    private int workspaceNumber;
    private int numConnections;
    @NotEmpty
    @NotNull
    private String crawlOwner;
}
