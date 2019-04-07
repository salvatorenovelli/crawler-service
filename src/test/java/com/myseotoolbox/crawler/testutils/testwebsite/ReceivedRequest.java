package com.myseotoolbox.crawler.testutils.testwebsite;

import lombok.Getter;
import org.eclipse.jetty.server.Request;

@Getter
public class ReceivedRequest {

    private final String userAgent;
    private final String url;

    ReceivedRequest(String userAgent, String url) {
        this.userAgent = userAgent;
        this.url = url;
    }


    public static ReceivedRequest from(Request request) {
        return new ReceivedRequest(request.getHeader("User-Agent"), request.getRequestURI());
    }

    public String getUrl() {
        return this.url;
    }
}
