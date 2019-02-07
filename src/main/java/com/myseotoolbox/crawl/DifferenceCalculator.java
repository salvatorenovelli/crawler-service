package com.myseotoolbox.crawl;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class DifferenceCalculator {

    public static int compare(List<String> ref, List<String> cur) {
        return equalsList(ref, cur) ? 0 : 1;
    }

    public static int compare(String ref, String cur) {
        return ref == null || Objects.equals(ref, cur) ? 0 : 1;
    }


    private static boolean equalsList(List<String> ref, List<String> cur) {

        if (ref == null) return true;

        ListIterator<String> refIterator = ref.listIterator();
        ListIterator<String> curIterator = cur.listIterator();

        while (refIterator.hasNext() && curIterator.hasNext()) {
            String refStr = refIterator.next();
            String curStr = curIterator.next();

            if (refStr != null && !refStr.equals(curStr)) return false;

        }
        return !(refIterator.hasNext() || curIterator.hasNext());
    }
}
