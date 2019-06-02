package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.myseotoolbox.crawler.MetaTagSanitizer.sanitize;
import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalized;


@Component
public class MonitoredUriUpdater {
    public static final boolean DONT_MATCH_SCHEMA = false;
    private final MongoOperations mongoOperations;
    private final WorkspaceRepository workspaceRepository;

    public MonitoredUriUpdater(MongoOperations operations, WorkspaceRepository workspaceRepository) {
        this.mongoOperations = operations;
        this.workspaceRepository = workspaceRepository;
    }

    public void updateCurrentValue(PageSnapshot snapshot) {

        //this is canonicalized to a different URL. No need to re-persist it. We'll crawl the canonical version and persist that separately
        if (isCanonicalized(snapshot)) return;

        sanitize(snapshot);

        workspaceRepository.findAll()
                .stream()
                .filter(workspace -> websiteUrlMatch(workspace.getWebsiteUrl(), snapshot.getUri()))
                .forEach(workspace -> {
                    Query query = new Query(new Criteria().andOperator(new Criteria("uri").is(snapshot.getUri()), new Criteria("workspaceNumber").is(workspace.getSeqNumber())));
                    Update update = new Update()
                            .set("uri", snapshot.getUri())
                            .set("workspaceNumber", workspace.getSeqNumber())
                            .set("currentValue", snapshot)
                            .set("lastScan", snapshot.getCreateDate());

                    mongoOperations.upsert(query, update, MonitoredUri.class);
                });

    }

    private boolean websiteUrlMatch(String origin, String uri) {
        return origin != null && WebsiteOriginUtils.isChildOf(URI.create(origin), URI.create(uri), DONT_MATCH_SCHEMA);
    }
}
