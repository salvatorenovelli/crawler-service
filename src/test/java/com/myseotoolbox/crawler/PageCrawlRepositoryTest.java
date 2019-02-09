package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.repository.PageCrawlRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.myseotoolbox.crawler.testutils.TestCalendarService.testDay;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@DataMongoTest
public class PageCrawlRepositoryTest {

    @Autowired PageCrawlRepository sut;


    @Test
    public void basicFind() {

        sut.save(new PageCrawl("http://uri1", testDay(1)));


        Optional<PageCrawl> lastCrawl = sut.findTopByUriOrderByCreateDateDesc("http://uri1");
        assertTrue(lastCrawl.isPresent());
        assertThat(lastCrawl.get().getCreateDate(), is(testDay(1)));
        assertThat(lastCrawl.get().getUri(), is("http://uri1"));
    }

    @Test
    public void testFindLastCrawl() {

        sut.save(new PageCrawl("http://uri", testDay(1)));
        sut.save(new PageCrawl("http://uri", testDay(2)));
        sut.save(new PageCrawl("http://uri", testDay(3)));


        Optional<PageCrawl> lastCrawl = sut.findTopByUriOrderByCreateDateDesc("http://uri");
        assertTrue(lastCrawl.isPresent());
        assertThat(lastCrawl.get().getCreateDate(), is(testDay(3)));
    }

    @Test
    public void testFilterByUri() {

        sut.save(new PageCrawl("http://uri1", testDay(1)));
        sut.save(new PageCrawl("http://uri1", testDay(2)));
        sut.save(new PageCrawl("http://uri1", testDay(3)));
        sut.save(new PageCrawl("http://uri2", testDay(4)));


        Optional<PageCrawl> lastCrawl = sut.findTopByUriOrderByCreateDateDesc("http://uri1");
        assertTrue(lastCrawl.isPresent());
        assertThat(lastCrawl.get().getCreateDate(), is(testDay(3)));
        assertThat(lastCrawl.get().getUri(), is("http://uri1"));
    }

    @Test
    public void returnEmptyIfThereAreNoCrawlsForTheUri() {
        sut.save(new PageCrawl("http://uri1", testDay(1)));

        Optional<PageCrawl> lastCrawl = sut.findTopByUriOrderByCreateDateDesc("http://differentUri");
        assertFalse(lastCrawl.isPresent());
    }

}