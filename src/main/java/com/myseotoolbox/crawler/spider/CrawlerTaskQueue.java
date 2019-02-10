package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;


@ThreadSafe
@Slf4j
class CrawlerTaskQueue {

    private final ReentrantLock lock = new ReentrantLock();

    private final Queue<URI> toVisit = new LinkedBlockingQueue<>();
    private final Set<URI> visited = new HashSet<>();
    private final Set<URI> inProgress = new HashSet<>();


    public CrawlerTaskQueue(List<URI> seeds) {
        toVisit.addAll(seeds);
    }

    /**
     * There are urls in {@code toVisit} or there are elements in {@code inProgress}, which may produce more URI, potentially.
     */
    public boolean mayHaveNext() {
        return toVisit.size() > 0 || inProgress.size() > 0;
    }

    /**
     * Return the next URI or null in case there's none left.
     * <p>
     * <p>
     * Blocks if the queue is empty but there is the possibility that more uri are added because there are in progress crawl by other threads
     * {@return The next element or {@code null} if none is available}
     */
    public URI take() {
        if (toVisit.isEmpty()) {
            return null;
        }
        URI poll = toVisit.poll();
        inProgress.add(poll);
        return poll;
    }


    public void onSnapshotComplete(URI uri, List<URI> links) {
        assertAbsolute(uri);
        inProgress.remove(uri);
        if (!visited.add(uri))
            throw new IllegalStateException("Already visited!");
        enqueueDiscoveredLinks(uri, links);
    }

    private void enqueueDiscoveredLinks(URI sourceUri, List<URI> links) {
        List<URI> newLinks = links.stream()
                .filter(this::alreadyVisited)
                .map(uri -> toAbsolute(sourceUri, uri))
                .collect(Collectors.toList());
        toVisit.addAll(newLinks);
    }

    private boolean alreadyVisited(URI uri) {
        return !visited.contains(uri);
    }

    private URI toAbsolute(URI sourceUri, URI uri) {
        if (uri.isAbsolute()) return uri;

        String path = uri.getPath();
        if (containsUnicodeCharacters(path)) {
            log.warn("Redirect destination {} contains non ASCII characters (as required by the standard)", path);
            return sourceUri.resolve(SafeStringEscaper.escapeString(path));
        } else {
            return sourceUri.resolve(path);
        }

    }

    private void assertAbsolute(URI uri) {
        if (!uri.isAbsolute()) throw new IllegalStateException("URI Should be absolute or we risk to visit it twice.");
    }
}
