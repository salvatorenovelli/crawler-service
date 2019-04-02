package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;

import java.util.Objects;

public class IsCanonicalized {

    /**
     * Return true if a snapshot has a canonical pointing on a different page than uri
     */
    public static boolean isCanonicalized(PageSnapshot snapshot) {
        return isValidCanonical(snapshot) &&
                !Objects.equals(snapshot.getCanonicals().get(0), snapshot.getUri());
    }

    private static boolean isValidCanonical(PageSnapshot snapshot) {
        return snapshot.getCanonicals() != null && snapshot.getCanonicals().size() > 0;
    }
}
