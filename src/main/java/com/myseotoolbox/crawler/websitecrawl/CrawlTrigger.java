package com.myseotoolbox.crawler.websitecrawl;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Collections;

import static com.myseotoolbox.crawler.websitecrawl.CrawlTrigger.Type.SCHEDULED;
import static com.myseotoolbox.crawler.websitecrawl.CrawlTrigger.Type.USER_INITIATED_WORKSPACE;

@Data
@Document
public class CrawlTrigger {
    public enum Type {
        SCHEDULED,
        USER_INITIATED_WORKSPACE;
    }

    private final Type type;
    private final Collection<Integer> targetWorkspaces;

    public static CrawlTrigger forScheduledCrawlTrigger(Collection<Integer> targetWorkspaces) {
        return new CrawlTrigger(SCHEDULED, Collections.unmodifiableCollection(targetWorkspaces));
    }

    public static CrawlTrigger forUserInitiatedWorkspaceCrawl(int workspaceNumber) {
        return new CrawlTrigger(USER_INITIATED_WORKSPACE, Collections.singletonList(workspaceNumber));
    }
}