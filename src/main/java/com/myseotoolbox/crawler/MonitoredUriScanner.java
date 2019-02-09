package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.MonitoredUriScraper;
import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static io.vavr.control.Try.run;

@Slf4j
@Component
public class MonitoredUriScanner {


    public static final int DEFAULT_PAGE_SIZE = 20;
    private final MonitoredUriScraper monitoredUriScraper;

    private final MonitoredUriRepository monitoredUriRepository;
    private final WorkspaceRepository workspaceRepository;
    @Qualifier("asyncMonitoredUriScanExecutor")
    private final Executor executor;

    public MonitoredUriScanner(MonitoredUriScraper monitoredUriScraper, MonitoredUriRepository monitoredUriRepository, WorkspaceRepository workspaceRepository, Executor executor) {
        this.monitoredUriScraper = monitoredUriScraper;
        this.monitoredUriRepository = monitoredUriRepository;
        this.workspaceRepository = workspaceRepository;
        this.executor = executor;
    }

    public void scanAll() {
        log.info("Scanning all Workspaces");

        workspaceRepository._getAll()
                .map(Workspace::getSeqNumber)
                .forEach(this::asyncScanWorkspace);
    }

    public void asyncScanWorkspace(int workspaceNumber) {
        executor.execute(() -> scanWorkspace(workspaceNumber));
    }

    public void scanSingleUri(String uri) throws EntityNotFoundException {
        log.info("Scanning single uri: {}", uri);

        Optional<MonitoredUri> byUri = monitoredUriRepository.findByUri(uri);
        if (!byUri.isPresent()) throw new EntityNotFoundException();
        scanUris(Stream.of(byUri.get()));
        log.info("Scanning single uri: {} completed.", uri);
    }

    private void scanWorkspace(int workspaceNumber) {

        log.info("Workspace {} - Start scan", workspaceNumber);

        long total = monitoredUriRepository.countByWorkspaceNumber(workspaceNumber);
        long numPages = DEFAULT_PAGE_SIZE == 0 ? 1 : (int) Math.ceil((double) total / (double) DEFAULT_PAGE_SIZE);

        log.info("Workspace {} - Found {} uris", workspaceNumber, total);

        for (int i = 0; i < numPages; i++) {
            log.info("Workspace {} - Scanning page {}/{}", workspaceNumber, i + 1, numPages);
            scanUris(monitoredUriRepository.findAllByWorkspaceNumber(workspaceNumber, PageRequest.of(i, DEFAULT_PAGE_SIZE)).stream());
        }

        log.info("Workspace {} - Scan completed", workspaceNumber, total);
    }

    private void scanUris(Stream<MonitoredUri> stream) {
        stream.map(monitoredUriScraper::crawlUri)
                .peek(it -> it.setStatus(it.getCurrentValue().getCrawlStatus()))
                .peek(uri -> log.debug("FinishedScanning {}. Persisting monitored URI", uri.getUri()))
                .forEach(monitoredUri ->
                        run(() -> monitoredUriRepository.save(monitoredUri))
                                .orElseRun(t -> log(monitoredUri, t))
                );
    }

    private void log(MonitoredUri monitoredUri, Throwable throwable) {
        log.error("Error while persisting " + monitoredUri, throwable);
    }
}
