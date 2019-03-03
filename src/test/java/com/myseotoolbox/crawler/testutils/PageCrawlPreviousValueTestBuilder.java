//package com.myseotoolbox.crawler.testutils;
//
//
//
//import com.myseotoolbox.crawler.model.MonitoredUri;
//import com.myseotoolbox.crawler.model.PageSnapshot;
//import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
//import com.myseotoolbox.crawler.repository.PageSnapshotRepository;
//
//import java.util.Collections;
//import java.util.Date;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//public class PageCrawlPreviousValueTestBuilder {
//
//    private static MonitoredUriRepository monitoredUriRepository;
//    private final String uri;
//    private PageSnapshot prevValue;
//
//    public PageCrawlPreviousValueTestBuilder(String uri) {
//        this.uri = uri;
//        this.prevValue = pageSnapshotForUri(uri, new Date());
//    }
//
//    public static void initMocks(MonitoredUriRepository mockMonitoredUriRepo, PageSnapshotRepository pageSnapshotRepository) {
//        PageCrawlPreviousValueTestBuilder.monitoredUriRepository = mockMonitoredUriRepo;
//        when(pageSnapshotRepository.save(any(PageSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));
//    }
//
//    public PageCrawlPreviousValueTestBuilder title(String s) {
//        this.prevValue.setTitle(s);
//        return this;
//    }
//
//    public void build() {
//        when(monitoredUriRepository.findByUri(uri)).thenReturn(Optional.of(buildLastScan()));
//    }
//
//    private MonitoredUri buildLastScan() {
//        MonitoredUri lastScanResult = new MonitoredUri();
//        lastScanResult.setUri(uri);
//        lastScanResult.setCurrentValue(prevValue);
//        return lastScanResult;
//    }
//
//    private PageSnapshot pageSnapshotForUri(String uri, Date createDate) {
//        PageSnapshot snapshot = new PageSnapshot(uri, "", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
//        snapshot.setRedirectChainElements(PageSnapshotTestBuilder.buildRedirectChainElementsFor(uri, 200));
//        snapshot.setCreateDate(createDate);
//        return snapshot;
//    }
//}
