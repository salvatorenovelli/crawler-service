package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;

import java.util.List;

public class GetDestinationUri {

    public static String getDestinationUri(PageSnapshot pageSnapshot) {
        List<RedirectChainElement> redirectChainElements = pageSnapshot.getRedirectChainElements();
        return redirectChainElements.get(redirectChainElements.size() - 1).getDestinationURI();
    }
}
