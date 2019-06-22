package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.PageCrawlRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalizedToDifferentUri;


@Component
public class PageCrawlPersistence {

    private final ArchiveServiceClient archiveClient;
    private final PageCrawlRepository pageCrawlRepository;
    private final PageCrawlBuilder builder = new PageCrawlBuilder();

    public PageCrawlPersistence(ArchiveServiceClient archiveClient, PageCrawlRepository pageCrawlRepository) {
        this.archiveClient = archiveClient;
        this.pageCrawlRepository = pageCrawlRepository;
    }

    public void persistPageCrawl(PageSnapshot curVal) {
        if (isCanonicalizedToDifferentUri(curVal)) return;

        Optional<PageSnapshot> prevValue = archiveClient.getLastPageSnapshot(curVal.getUri());
        persistPageCrawl(prevValue.orElse(null), curVal);
    }

    private void persistPageCrawl(@Nullable PageSnapshot prevValue, PageSnapshot curValue) {

        Optional<PageCrawl> lastCrawl = pageCrawlRepository.findTopByUriOrderByCreateDateDesc(curValue.getUri());
        PageCrawl build = builder.build(prevValue, curValue, lastCrawl.orElse(null));
        pageCrawlRepository.save(build);

    }
}
