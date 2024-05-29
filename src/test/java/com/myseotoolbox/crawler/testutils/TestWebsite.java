package com.myseotoolbox.crawler.testutils;

import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;

import java.util.List;

public interface TestWebsite {
    List<ReceivedRequest> getRequestsReceived();

    List<String> getRequestsReceivedAsUrls();
}
