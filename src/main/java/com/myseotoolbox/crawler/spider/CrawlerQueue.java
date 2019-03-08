package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;
import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@ThreadSafe
class CrawlerQueue implements Consumer<PageSnapshot> {

    private final Set<URI> visited = new HashSet<>();
    private final Set<URI> inProgress = new HashSet<>();
    private final List<URI> seeds = new ArrayList<>();

    private final Consumer<SnapshotTask> crawlersPool;
    private final UriFilter uriFilter;
    private final List<Consumer<PageSnapshot>> onSnapshotListeners = new ArrayList<>();
    private final PageLinksHelper helper = new PageLinksHelper();

    public CrawlerQueue(List<URI> seeds, Consumer<SnapshotTask> crawlersPool, UriFilter filter) {
        this.crawlersPool = crawlersPool;
        this.uriFilter = filter;
        this.seeds.addAll(seeds);
    }

    public synchronized void subscribeToPageCrawled(Consumer<PageSnapshot> subscriber) {
        onSnapshotListeners.add(subscriber);
    }

    public synchronized void start() {
        submitTasks(seeds);
    }

    @Override
    public void accept(PageSnapshot snapshot) {
        log.info("Scanned: {} links:{}", snapshot.getUri(), snapshot.getLinks() != null ? snapshot.getLinks().size() : 0);
        notifyListeners(snapshot);
        onScanCompleted(URI.create(snapshot.getUri()), helper.filterValidLinks(snapshot.getLinks()));
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
        if (links.size() > 0) {
            submitTasks(newLinks);
        }
    }

    private synchronized void submitTasks(List<URI> seeds) {
        inProgress.addAll(seeds);
        seeds.stream()
                .map(uri -> new SnapshotTask(uri, this))
                .forEach(crawlersPool);
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

