package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CrawlStartedEventListenerTest {

    @InjectMocks CrawlStartedEventListener sut;

    @Mock private WebsiteCrawlRepository websiteCrawlRepository;
    @Mock private WebsiteCrawl websiteCrawl;

    @Test
    public void onCrawlStartedEventShouldPersistCrawlStartedEvent() {
        CrawlStartedEvent event = new CrawlStartedEvent(WebsiteCrawlFactory.newWebsiteCrawlFor("origin123", Collections.emptyList()));
        sut.onCrawlStartedEvent(event);
        verify(websiteCrawlRepository).save(argThat(websiteCrawl -> websiteCrawl.getOrigin().equals("origin123")));
    }
}