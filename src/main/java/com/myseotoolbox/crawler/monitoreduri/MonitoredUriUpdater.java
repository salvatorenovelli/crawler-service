package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.sitemap.SitemapRepository;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.myseotoolbox.crawler.spider.PathMatcher.isSubPath;
import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoredUriUpdater {
    private final MongoOperations mongoOperations;
    private final WorkspaceRepository workspaceRepository;
    private final SitemapRepository sitemapRepository;


    public void updateCurrentValue(WebsiteCrawl websiteCrawl, PageSnapshot snapshot) {

        workspaceRepository.findAll()
                .stream()
                .filter(workspace -> websiteUrlMatch(workspace.getWebsiteUrl(), websiteCrawl.getOrigin(), snapshot.getUri()))
                .forEach(workspace -> {
                    Query query = new Query(
                            new Criteria().andOperator(
                                    new Criteria("uri").is(snapshot.getUri()),
                                    new Criteria("workspaceNumber").is(workspace.getSeqNumber())
                            )
                    );
                    Update update = new Update()
                            .set("uri", snapshot.getUri())
                            .set("workspaceNumber", workspace.getSeqNumber())
                            .set("currentValue", snapshot)
                            .unset("lastCrawl.inboundLinksCount.internal")
                            .set("lastCrawl.inboundLinks.internal.SITEMAP", sitemapRepository.findSitemapsLinkingTo(websiteCrawl, snapshot.getUri()))
                            .set("lastCrawl.websiteCrawlId", websiteCrawl.getId().toHexString())
                            .set("lastCrawl.dateTime", snapshot.getCreateDate());

                    mongoOperations.upsert(query, update, MonitoredUri.class);
                });

    }


    /**
     * We verify that crawl origin matches workspace origin to make sure that whatever we discover during crawl
     * can only be persisted on workspaces that has matching origin
     */

    private boolean websiteUrlMatch(String workspaceOrigin, String crawlOrigin, String snapshotUrl) {

        if (workspaceOrigin == null || StringUtils.isEmpty(workspaceOrigin)) return false;
        URI workspaceOriginUri = URI.create(workspaceOrigin);
        URI crawlOriginUri = URI.create(crawlOrigin);
        URI snapshotUri = URI.create(snapshotUrl);

        boolean sameCrawlOrigin = isSameOrigin(crawlOriginUri, workspaceOriginUri, false); //check schema
        boolean hostMatching = isHostMatching(workspaceOriginUri, snapshotUri, false); //ignore schema
        boolean subPath = isSubPath(workspaceOriginUri.getPath(), snapshotUri.getPath());

        return sameCrawlOrigin && hostMatching && subPath;
    }

}
