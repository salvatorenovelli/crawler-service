package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;

@Slf4j
@ThreadSafe
class CrawlerTaskQueue {

    private final Set<URI> visited = new HashSet<>();
    private final Set<URI> inProgress = new HashSet<>();
    private final List<URI> seeds = new ArrayList<>();
    private final CrawlersPool pool;


    public CrawlerTaskQueue(List<URI> seeds, CrawlersPool pool) {
        this.pool = pool;
        this.seeds.addAll(seeds);
    }

    public synchronized void start() {
        try {
            submitTasks(seeds);

            while (inProgress.size() > 0) {
                this.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void onNewLinksDiscovered(URI baseUri, List<URI> links) {
        assertAbsolute(baseUri);
        if (!inProgress.remove(baseUri))
            throw new IllegalStateException("Completing snapshot of not in progress URI:" + baseUri + " (could be already completed or never submitted)");
        if (!visited.add(baseUri))
            throw new IllegalStateException("Already visited: " + baseUri);
        enqueueDiscoveredLinks(baseUri, links);
        this.notify();
    }

    private synchronized void enqueueDiscoveredLinks(URI sourceUri, List<URI> links) {
        List<URI> newLinks = links.stream()
                .filter(this::alreadyVisited)
                .map(uri -> toAbsolute(sourceUri, uri))
                .collect(Collectors.toList());
        if (links.size() > 0) {
            submitTasks(newLinks);
        }
    }

    private synchronized void submitTasks(List<URI> seeds) {
        inProgress.addAll(seeds);
        seeds.forEach(pool::submit);
    }

    private synchronized boolean alreadyVisited(URI uri) {
        return !visited.contains(uri);
    }

    private static URI toAbsolute(URI sourceUri, URI uri) {
        if (uri.isAbsolute()) return uri;

        String path = uri.getPath();
        if (containsUnicodeCharacters(path)) {
            log.warn("Redirect destination {} contains non ASCII characters (as required by the standard)", path);
            return sourceUri.resolve(SafeStringEscaper.escapeString(path));
        } else {
            return sourceUri.resolve(path);
        }

    }

    private static void assertAbsolute(URI uri) {
        if (!uri.isAbsolute()) throw new IllegalStateException("URI Should be absolute or we risk to visit it twice.");
    }
}

