package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;

import java.net.URI;
import java.util.List;

public class GetDestinationUri {

    public static String getDestinationUriString(PageSnapshot pageSnapshot) {
        List<RedirectChainElement> redirectChainElements = pageSnapshot.getRedirectChainElements();
        return redirectChainElements.get(redirectChainElements.size() - 1).getDestinationURI();
    }

    public static URI getDestinationUri(PageSnapshot pageSnapshot) {
        return URI.create(getDestinationUriString(pageSnapshot));
    }
}
