package com.myseotoolbox.crawler.testutils;


import com.myseotoolbox.crawler.model.AnalyticsSettings;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class WorkspaceBuilder {

    private final WorkspaceRepository workspaceRepository;
    private final boolean repoIsMock;
    private Workspace current;

    private final List<Workspace> workspaces = new ArrayList<>();

    public WorkspaceBuilder(WorkspaceRepository repo) {
        this(repo, false);
    }

    public WorkspaceBuilder(WorkspaceRepository workspaceRepository, boolean repoIsMock) {
        this.workspaceRepository = workspaceRepository;
        this.repoIsMock = repoIsMock;
    }

    public WorkspaceBuilder givenWorkspace(int wsNumber) {
        current = createWorkspace(wsNumber);
        return this;
    }

    public void build() {
        if (repoIsMock) {
            when(workspaceRepository._getAll()).thenReturn(workspaces.stream());
        }
        and();
    }

    public static Workspace createWorkspace(int wsNumber) {
        Workspace entity = new Workspace();
        entity.setSeqNumber(wsNumber);
        return entity;
    }

    public WorkspaceBuilder and() {
        if (!repoIsMock) {
            workspaceRepository.save(current);
        } else {
            workspaces.add(current);
        }
        current = null;
        return this;
    }

    public WorkspaceBuilder withAnalyticsSettings(String tokenId, String viewId) {
        this.current.setAnalyticsSettings(new AnalyticsSettings(tokenId, viewId));
        return this;
    }
}
