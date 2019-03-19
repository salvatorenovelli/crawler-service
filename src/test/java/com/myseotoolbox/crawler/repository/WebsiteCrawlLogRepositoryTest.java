package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@DataMongoTest
public class WebsiteCrawlLogRepositoryTest {


    @Autowired
    WebsiteCrawlLogRepository sut;

    LocalDate now = LocalDate.now();


    @Test
    public void shouldFindLast() {
        sut.save(new WebsiteCrawlLog("origin", now.minusDays(3)));
        sut.save(new WebsiteCrawlLog("origin", now.minusDays(2)));
        sut.save(new WebsiteCrawlLog("origin", now.minusDays(1)));

        assertThat(sut.findTopByOriginOrderByDateDesc("origin").get().getDate(), is(now.minusDays(1)));
    }

    @Test
    public void shouldFilterByOrigin() {
        sut.save(new WebsiteCrawlLog("origin1", now.minusDays(3)));
        sut.save(new WebsiteCrawlLog("origin2", now.minusDays(3)));

        assertThat(sut.findTopByOriginOrderByDateDesc("origin1").get().getOrigin(), is("origin1"));
    }
}