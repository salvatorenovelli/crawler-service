package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.pagelinks.PageLink;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.testutils.IsolatedMongoDbTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;


public class PageSnapshotRepositoryTest extends IsolatedMongoDbTest {

    @Autowired
    PageSnapshotRepository sut;

    @Test
    public void shouldNotPersistLinks() {
        String uri = "http://uri1";
        PageSnapshot snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(uri);
        snapshot.setLinks(Collections.singletonList(new PageLink("http://uri1/dst1", Collections.emptyMap())));

        sut.save(snapshot);

        Collection<PageSnapshot> persisted = sut.findByUri(uri);
        assertFalse(persisted.isEmpty());
        assertNull(persisted.iterator().next().getLinks());
    }
}