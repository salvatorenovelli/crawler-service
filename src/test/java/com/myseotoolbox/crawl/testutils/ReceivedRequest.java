package com.myseotoolbox.crawl.testutils;

import lombok.Getter;
import org.eclipse.jetty.server.Request;

@Getter
public class ReceivedRequest {

    private final String userAgent;

    ReceivedRequest(String userAgent) {this.userAgent = userAgent;}


    public static ReceivedRequest from(Request request) {
        return new ReceivedRequest(request.getHeader("User-Agent"));
    }
}
