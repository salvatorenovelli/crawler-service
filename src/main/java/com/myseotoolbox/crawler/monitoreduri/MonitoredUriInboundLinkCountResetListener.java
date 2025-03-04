package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

/**
 * <p>This service reset the inbound link count associated with monitored uri, when they become orphaned. (are not discovered in the last crawl)</p>
 * <p> MonitoredUri are a flat snapshot of the last crawl + orphan pages.
 * We persist inbound link count into Monitored uri because calculating it is computationally expensive.
 * Inbound link count service takes care of updating the count of incoming links after a crawl is finished by
 * aggregating data from ouboundLinks collection. The problem is, MonitoredUris that don't have any inbound links (when they become orphaned)
 * are not updated and would show the number of inbound link associated with a previous crawl, which would be confusing. </p>
 *
 * <p>We could, real time, filter the inbound link count, only if associated with the last crawl, but since MonitoredUri is also used to export to excel
 * and given the fact that they represent a snapshot of the last crawl, this service will reset the inbound link count, to all the
 * monitoredUri that have not been crawled by the last crawl.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoredUriInboundLinkCountResetListener {

    private final MongoOperations mongoOperations;

    @EventListener
    public void onWebsiteCrawlCompletedEvent(WebsiteCrawlCompletedEvent event) {
        BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, MonitoredUri.class);

        getTargetWorkspaces(event).forEach(workspace -> {
            log.debug("Resetting inbound link count for workspace: {}", workspace);

            Query query = query(where("workspaceNumber").is(workspace)
                    .and("lastCrawl.websiteCrawlId").ne(event.websiteCrawl().getId().toHexString()));

            bulkOps.updateMulti(query, update("lastCrawl.inboundLinksCount.internal.ahref", 0));
        });

        if (!getTargetWorkspaces(event).isEmpty()) bulkOps.execute();
    }

    private static Collection<Integer> getTargetWorkspaces(WebsiteCrawlCompletedEvent event) {
        return (event.websiteCrawl().getTrigger() != null && event.websiteCrawl().getTrigger().getTargetWorkspaces() != null)
                ? event.websiteCrawl().getTrigger().getTargetWorkspaces()
                : List.of();
    }

}
