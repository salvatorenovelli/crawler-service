package com.myseotoolbox.crawler.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
