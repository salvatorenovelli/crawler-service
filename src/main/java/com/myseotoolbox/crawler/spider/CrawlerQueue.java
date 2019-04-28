package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.SnapshotResult;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import com.myseotoolbox.crawler.utils.LoggingUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;
import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;
import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalized;

@Slf4j
@ThreadSafe
class CrawlerQueue implements Consumer<SnapshotResult> {

    private final Set<URI> visited = new HashSet<>();
    private final Set<URI> inProgress = new HashSet<>();
    private final List<URI> seeds = new ArrayList<>();

    private final CrawlersPool crawlersPool;
    private final UriFilter uriFilter;
    private final List<Consumer<PageSnapshot>> onSnapshotListeners = new ArrayList<>();
    private final PageLinksHelper helper = new PageLinksHelper();

    private final int maxCrawls;
    private final String queueName;

    public CrawlerQueue(String queueName, List<URI> seeds, CrawlersPool crawlersPool, UriFilter filter, int maxCrawls) {
        this.queueName = queueName;
        this.crawlersPool = crawlersPool;
        this.uriFilter = filter;
        this.maxCrawls = maxCrawls;
        this.seeds.addAll(seeds);
    }

    public synchronized void subscribeToPageCrawled(Consumer<PageSnapshot> subscriber) {
        onSnapshotListeners.add(subscriber);
    }

    public synchronized void start() {
        submitTasks(seeds);
    }

    @Override
    public void accept(SnapshotResult result) {
        String sourceUri = result.getUri();
        List<URI> links = result.isBlockedChain() ? Collections.emptyList() : discoverLinks(result.getPageSnapshot());

        log.trace("Scanned: {} links:{}", sourceUri, links.size());

        if (!result.isBlockedChain()) {
            notifyListeners(result.getPageSnapshot());
        }
        onScanCompleted(URI.create(sourceUri), links);
    }

    private List<URI> discoverLinks(PageSnapshot snapshot) {
        List<URI> links = helper.filterValidLinks(snapshot.getLinks());

        if (isCanonicalized(snapshot))
            links.addAll(helper.filterValidLinks(snapshot.getCanonicals()));
        return links;
    }

    private synchronized void onScanCompleted(URI baseUri, List<URI> links) {
        assertAbsolute(baseUri);
        if (!inProgress.remove(baseUri))
            throw new IllegalStateException("Completing snapshot of not in progress URI:" + baseUri + " (could be already completed or never submitted)");
        if (!visited.add(baseUri))
            throw new IllegalStateException("Already visited: " + baseUri);
        enqueueDiscoveredLinks(baseUri, links);
    }

    private synchronized void enqueueDiscoveredLinks(URI sourceUri, List<URI> links) {
        List<URI> newLinks = links.stream()
                .map(uri -> toAbsolute(sourceUri, uri))
                .filter(uri -> uriFilter.shouldCrawl(sourceUri, uri))
                .filter(uri -> !alreadyVisited(uri))
                .distinct()
                .collect(Collectors.toList());


        submitTasks(newLinks);

    }

    private synchronized void submitTasks(List<URI> seeds) {

        List<URI> allowedSeeds = calculateAllowedSeeds(seeds);

        if (allowedSeeds.size() > 0) {
            inProgress.addAll(allowedSeeds);
            allowedSeeds.stream()
                    .map(uri -> new SnapshotTask(uri, this))
                    .forEach(crawlersPool::accept);
        } else {
            if (inProgress.size() == 0) {
                crawlersPool.shutDown();
            }
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
            log.warn("Redirect destination {} contains non ASCII characters (as required by the standard)", path);
            return sourceUri.resolve(SafeStringEscaper.escapeString(path));
        } else {
            try {
                return sourceUri.resolve(path);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private static void assertAbsolute(URI uri) {
        if (!uri.isAbsolute()) throw new IllegalStateException("URI should be absolute or we risk to visit it twice.");
    }

    private void notifyListeners(PageSnapshot snapshot) {
        onSnapshotListeners.forEach(listener -> runOrLogWarning(() -> listener.accept(snapshot), "Unable to notify listener"));
    }
}

