package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebsiteCrawlRepositoryPersistenceListenerTest {

    @InjectMocks WebsiteCrawlRepositoryPersistenceListener sut;

    @Mock private WebsiteCrawlRepository websiteCrawlRepository;
    @Mock private WebsiteCrawl websiteCrawl;

    @Test
    public void persistOnRepository() {
        CrawlStartedEvent event = new CrawlStartedEvent(WebsiteCrawlFactory.newWebsiteCrawlFor("origin123", Collections.emptyList()));
        sut.persistOnRepository(event);
        verify(websiteCrawlRepository).save(argThat(websiteCrawl -> websiteCrawl.getOrigin().equals("origin123")));
    }
}