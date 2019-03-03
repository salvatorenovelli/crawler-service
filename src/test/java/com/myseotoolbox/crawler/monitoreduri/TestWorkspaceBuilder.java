package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;

class TestWorkspaceBuilder {
    public static final String DEFAULT_TEST_WEBSITE_URL = "http://host";
    private static final String DEFAULT_OWNER = "user340790";
    private final WorkspaceRepository workspaceRepository;
    private MonitoredUriUpdaterTest monitoredUriUpdaterTest;
    private final int seqNumber;
    private String websiteUrl = DEFAULT_TEST_WEBSITE_URL;
    private String ownerName = DEFAULT_OWNER;

    public TestWorkspaceBuilder(WorkspaceRepository workspaceRepository, MonitoredUriUpdaterTest monitoredUriUpdaterTest, int seqNumber) {
        this.workspaceRepository = workspaceRepository;
        this.monitoredUriUpdaterTest = monitoredUriUpdaterTest;
        this.seqNumber = seqNumber;
    }

    public TestWorkspaceBuilder withWebsiteUrl(String url) {
        this.websiteUrl = url;
        return this;
    }

    public void save() {

        Workspace entity = new Workspace();
        entity.setSeqNumber(seqNumber);
        entity.setWebsiteUrl(websiteUrl);
        entity.setOwnerName(ownerName);
        workspaceRepository.save(entity);
    }

    public TestWorkspaceBuilder withOwner(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }
}
