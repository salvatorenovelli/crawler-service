package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.PageCrawlRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.myseotoolbox.crawler.MetaTagSanitizer.sanitize;


@Component
public class PageCrawlPersistence {

    private final PageCrawlRepository pageCrawlRepository;
    private PageCrawlBuilder builder = new PageCrawlBuilder();

    public PageCrawlPersistence(PageCrawlRepository pageCrawlRepository) {
        this.pageCrawlRepository = pageCrawlRepository;
    }

    public void persistPageCrawl(@Nullable PageSnapshot prevValue, PageSnapshot curValue) {

        sanitize(curValue);

        Optional<PageCrawl> lastCrawl = pageCrawlRepository.findTopByUriOrderByCreateDateDesc(curValue.getUri());
        PageCrawl build = builder.build(prevValue, curValue, lastCrawl.orElse(null));
        pageCrawlRepository.save(build);

    }
}
