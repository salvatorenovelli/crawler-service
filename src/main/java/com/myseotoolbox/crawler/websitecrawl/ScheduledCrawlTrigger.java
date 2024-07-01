package com.myseotoolbox.crawler.websitecrawl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ScheduledCrawlTrigger extends CrawlTrigger {
    private Collection<Integer> targetWorkspaces;

    public ScheduledCrawlTrigger(Collection<Integer> targetWorkspaces) {
        super(Type.SCHEDULED);
        this.targetWorkspaces = Collections.unmodifiableCollection(targetWorkspaces);
    }
}