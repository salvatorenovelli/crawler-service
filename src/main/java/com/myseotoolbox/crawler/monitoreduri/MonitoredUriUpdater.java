package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

import static com.myseotoolbox.crawler.MetaTagSanitizer.sanitize;
import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isChildOf;
import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalized;


@Component
@Slf4j
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
        if (isCanonicalized(snapshot)) {
            log.debug("Skipping persistence of {} as it's canonicalized to {}", snapshot.getUri(), snapshot.getCanonicals());
            return;
        }

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

        if (origin == null || StringUtils.isEmpty(origin)) return false;

        URI originUri = URI.create(origin);
        URI possibleChildUri = URI.create(uri);

        URI alternateOrigin = getAlternateOrigin(originUri);

        return isChildOf(originUri, possibleChildUri, DONT_MATCH_SCHEMA) ||
                isChildOf(alternateOrigin, possibleChildUri, DONT_MATCH_SCHEMA);
    }

    private URI getAlternateOrigin(URI originUri) {

        if (originUri.getHost().startsWith("www.")) {
            return alterHost(originUri, originUri.getHost().substring(4));
        }

        return alterHost(originUri, "www." + originUri.getHost());
    }

    private URI alterHost(URI originUri, String newDomain) {
        try {
            return new URI(originUri.getScheme(), originUri.getUserInfo(), newDomain, originUri.getPort(), originUri.getPath(), originUri.getQuery(), originUri.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
