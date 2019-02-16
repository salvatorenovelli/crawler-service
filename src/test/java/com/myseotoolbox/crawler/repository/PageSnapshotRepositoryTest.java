package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;


@RunWith(SpringRunner.class)
@DataMongoTest
public class PageSnapshotRepositoryTest {

    @Autowired
    PageSnapshotRepository sut;

    @Test
    public void shouldNotPersistLinks() {
        String uri = "http://uri1";
        PageSnapshot snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(uri);
        snapshot.setLinks(Arrays.asList("http://uri1/dst1"));

        sut.save(snapshot);

        Collection<PageSnapshot> persisted = sut.findByUri(uri);
        assertFalse(persisted.isEmpty());
        assertNull(persisted.iterator().next().getLinks());
    }
}