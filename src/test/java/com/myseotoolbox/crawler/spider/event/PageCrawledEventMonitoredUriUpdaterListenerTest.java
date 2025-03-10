package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.myseotoolbox.crawler.spider.event.PageCrawledEventTestBuilder.aPageCrawledEvent;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PageCrawledEventMonitoredUriUpdaterListenerTest {

    @InjectMocks PageCrawledEventMonitoredUriUpdaterListener sut;
    @Mock private MonitoredUriUpdater monitoredUriUpdater;


    @Test
    public void onPageCrawledEventShouldUpdateMonitoredUri() {
        PageCrawledEvent event = aPageCrawledEvent("http://origin").withStandardValuesForPath("/dst").build();
        sut.onPageCrawledEvent(event);
        verify(monitoredUriUpdater).updateCurrentValue(
                event.websiteCrawl(),
                event.crawlResult().getPageSnapshot(),
                event.sitemapInboundLinks());
    }

}