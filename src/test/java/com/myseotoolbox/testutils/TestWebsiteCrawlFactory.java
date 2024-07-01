package com.myseotoolbox.testutils;

import com.myseotoolbox.crawler.websitecrawl.UserInitiatedWorkspaceCrawlTrigger;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;

import java.net.URI;
import java.util.Collection;

public class TestWebsiteCrawlFactory {
    public static final String TEST_OWNER = "unitTest@myseotoolbox.com";

    public static WebsiteCrawl newWebsiteCrawlFor(String testOrigin, Collection<URI> seeds) {
        return WebsiteCrawlFactory.newWebsiteCrawlFor(TEST_OWNER, new UserInitiatedWorkspaceCrawlTrigger(3862381), testOrigin, seeds);
    }

}
