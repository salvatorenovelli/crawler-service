package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@DataMongoTest
public class WorkspaceRepositoryTest {

    @Autowired
    MongoOperations operations;

    @Autowired
    WorkspaceRepository sut;

    @After
    public void tearDown() throws Exception {
        sut.deleteAll();
    }

    @Test
    public void crawlerSettingsCanHandleEmptyCrawlInterval() {
        Update update = new Update();

        update.set("seqNumber", 123);
        update.set("crawlerSettings.crawlEnabled", true);
        operations.upsert(new Query(), update, Workspace.class);

        List<Workspace> all = sut.findAll();

        assertThat(all, hasSize(1));
        assertThat(all.get(0).getCrawlerSettings().isCrawlEnabled(), is(true));
        assertThat(all.get(0).getCrawlerSettings().getCrawlIntervalDays(), is(MIN_CRAWL_INTERVAL));
    }

    @Test
    public void shouldSanitizeCrawlerSettingsMins() {
        Workspace workspace = new Workspace();
        CrawlerSettings crawlerSettings = new CrawlerSettings(-100, true, -10, 0L, null, DEFAULT_MAX_URL_PER_CRAWL);
        workspace.setCrawlerSettings(crawlerSettings);


        Workspace saved = sut.save(workspace);

        CrawlerSettings settings = saved.getCrawlerSettings();
        assertThat(settings.getCrawlIntervalDays(), is(MIN_CRAWL_INTERVAL));
        assertThat(settings.getMaxConcurrentConnections(), is(MIN_CONCURRENT_CONNECTIONS));
    }

    @Test
    public void shouldSanitizeCrawlerSettingsMaxs() {
        Workspace workspace = new Workspace();
        CrawlerSettings crawlerSettings = new CrawlerSettings(559849351, true, 578749, 0L, null, DEFAULT_MAX_URL_PER_CRAWL);
        workspace.setCrawlerSettings(crawlerSettings);


        Workspace saved = sut.save(workspace);

        CrawlerSettings settings = saved.getCrawlerSettings();
        assertThat(settings.getCrawlIntervalDays(), is(MAX_CRAWL_INTERVAL));
        assertThat(settings.getMaxConcurrentConnections(), is(MAX_CONCURRENT_CONNECTIONS));
    }
}