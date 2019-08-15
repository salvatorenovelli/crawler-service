package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import com.myseotoolbox.crawler.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;
import static com.myseotoolbox.crawler.spider.PageLinksHelper.MAX_URL_LEN;
import static com.myseotoolbox.crawler.utils.GetDestinationUri.getDestinationUri;
import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalizedToDifferentUri;

@Slf4j
@ThreadSafe
class CrawlerQueue implements Consumer<CrawlResult> {

    private final Set<URI> visited = new HashSet<>();
    private final Set<URI> inProgress = new HashSet<>();
    private final List<URI> seeds = new ArrayList<>();

    private final CrawlersPool crawlersPool;
    private final UriFilter uriFilter;
    private final CrawlEventDispatch dispatch;
    private final PageLinksHelper helper = new PageLinksHelper();

    private final int maxCrawls;
    private final String queueName;
    private boolean crawlShutdownInvoked = false;

    public CrawlerQueue(String queueName, Collection<URI> seeds, CrawlersPool crawlersPool, UriFilter filter, int maxCrawls, CrawlEventDispatch dispatch) {
        this.queueName = queueName;
        this.crawlersPool = crawlersPool;
        this.uriFilter = filter;
        this.maxCrawls = maxCrawls;
        this.dispatch = dispatch;
        this.seeds.addAll(seeds.stream().distinct().collect(Collectors.toList()));
    }

    public synchronized void start() {
        submitTasks(seeds);
    }

    @Override
    public void accept(CrawlResult result) {

        if (!result.isBlockedChain()) {
            notifyPageCrawled(result);
        } else {
            log.debug("Skipping crawl notification for {} because result is blockedChain: {}", result.getUri(), result.getChain());
        }

        onScanCompleted(result);
    }

    private List<URI> discoverLinks(PageSnapshot snapshot) {
        List<URI> links = helper.filterValidLinks(snapshot.getLinks());

        if (isCanonicalizedToDifferentUri(snapshot))
            links.addAll(helper.filterValidLinks(snapshot.getCanonicals()));
        return links;
    }

    private synchronized void onScanCompleted(CrawlResult crawlResult) {

        URI baseUri = URI.create(crawlResult.getUri());

        assertAbsolute(baseUri);
        if (!inProgress.remove(baseUri))
            throw new IllegalStateException("Completing snapshot of not in progress URI:" + baseUri + " (could be already completed or never submitted)");
        if (!visited.add(baseUri))
            throw new IllegalStateException("Already visited: " + baseUri);

        enqueueDiscoveredLinks(crawlResult);

        if (crawlCompleted()) {
            shutdown();
        }
    }

    private synchronized void enqueueDiscoveredLinks(CrawlResult crawlResult) {

        if (crawlResult.isBlockedChain()) return;

        PageSnapshot pageSnapshot = crawlResult.getPageSnapshot();
        String sourceUri = crawlResult.getUri();

        List<URI> links = discoverLinks(pageSnapshot);

        if (!links.isEmpty()) {
            log.debug("Scanned: {} links:{}", sourceUri, links.size());
            URI destinationUri = getDestinationUri(pageSnapshot);

            List<URI> newLinks = links.stream()
                    .map(uri -> toAbsolute(destinationUri, uri))
                    .filter(Objects::nonNull)
                    .filter(uri -> uri.toString().length() < MAX_URL_LEN)
                    .filter(uri -> uriFilter.shouldCrawl(destinationUri, uri))
                    .filter(uri -> !alreadyVisited(uri))
                    .distinct()
                    .collect(Collectors.toList());

            submitTasks(newLinks);
        }

    }

    private synchronized void submitTasks(List<URI> seeds) {
        List<URI> allowedSeeds = calculateAllowedSeeds(seeds);
        if (allowedSeeds.size() > 0) {
            inProgress.addAll(allowedSeeds);
            allowedSeeds.stream()
                    .map(uri -> new SnapshotTask(uri, this))
                    .forEach(crawlersPool::accept);
        }
    }

    private List<URI> calculateAllowedSeeds(List<URI> seeds) {

        int totUrlEnqueued = inProgress.size() + visited.size();

        if (totUrlEnqueued >= this.maxCrawls) {
            LoggingUtils.logWarningOnce(this, log, "Unable to enqueue more URL. Max size exceeded for " + this.queueName);
            return Collections.emptyList();
        }

        if (seeds.size() + totUrlEnqueued >= this.maxCrawls) {
            int remaining = this.maxCrawls - totUrlEnqueued;
            return seeds.subList(0, remaining);
        }

        return seeds;

    }

    private synchronized boolean alreadyVisited(URI uri) {
        return visited.contains(uri) || inProgress.contains(uri);
    }

    private static URI toAbsolute(URI sourceUri, URI uri) {
        if (uri.isAbsolute()) return uri;

        String path = uri.toString();
        if (containsUnicodeCharacters(path)) {
            log.debug("Redirect destination {} contains non ASCII characters (as required by the standard)", path);
            return sourceUri.resolve(SafeStringEscaper.escapeString(path));
        } else {
            try {
                return sourceUri.resolve(path);
            } catch (IllegalArgumentException e) {
                log.warn("Error while converting to absolute '{}' (source: '{}'). {}", uri, sourceUri, e.getMessage());
                return null;
            }
        }

    }

    private static void assertAbsolute(URI uri) {
        if (!uri.isAbsolute()) throw new IllegalStateException("URI should be absolute or we risk to visit it twice.");
    }

    private void notifyPageCrawled(CrawlResult crawlResult) {
        dispatch.pageCrawled(crawlResult);
    }

    private synchronized void shutdown() {
        if (!crawlShutdownInvoked) {
            crawlShutdownInvoked = true;
            crawlersPool.shutDown();
            dispatch.crawlEnded();
        }
    }

    private synchronized boolean crawlCompleted() {
        return inProgress.size() == 0;
    }
}

