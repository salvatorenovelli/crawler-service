package com.myseotoolbox.crawler.websitecrawl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserInitiatedWorkspaceCrawlTrigger extends CrawlTrigger {
    private int targetWorkspace;

    public UserInitiatedWorkspaceCrawlTrigger(int targetWorkspace) {
        super(Type.USER_INITIATED_WORKSPACE);
        this.targetWorkspace = targetWorkspace;
    }
}