package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;

import java.util.List;
import java.util.Objects;

public class IsCanonicalized {

    /**
     * Return true if a snapshot has a canonical pointing on a different page than uri
     */
    public static boolean isCanonicalizedToDifferentUri(PageSnapshot snapshot) {
        return isValidCanonical(snapshot) &&
                !Objects.equals(getFirstCanonical(snapshot), getDestinationUri(snapshot));
    }

    private static String getDestinationUri(PageSnapshot pageSnapshot) {
        List<RedirectChainElement> redirectChainElements = pageSnapshot.getRedirectChainElements();
        if (redirectChainElements == null || redirectChainElements.size() == 0) return getFirstCanonical(pageSnapshot);
        return redirectChainElements.get(redirectChainElements.size() - 1).getDestinationURI();
    }

    private static boolean isValidCanonical(PageSnapshot snapshot) {
        return snapshot.getCanonicals() != null && snapshot.getCanonicals().size() > 0;
    }

    private static String getFirstCanonical(PageSnapshot snapshot) {
        return snapshot.getCanonicals().get(0);
    }
}
