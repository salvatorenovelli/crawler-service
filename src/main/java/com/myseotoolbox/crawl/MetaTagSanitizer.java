package com.myseotoolbox.crawl;

import com.myseotoolbox.crawl.model.PageSnapshot;
import org.apache.commons.lang.StringEscapeUtils;


import java.util.List;
import java.util.stream.Collectors;

public class MetaTagSanitizer {



    public static void sanitize(PageSnapshot source) {
        if (source != null) {
            source.setTitle(sanitize(source.getTitle()));
            source.setMetaDescriptions(sanitize(source.getMetaDescriptions()));
            source.setH1s(sanitize(source.getH1s()));
            source.setH2s(sanitize(source.getH2s()));
        }
    }

    private static List<String> sanitize(List<String> input) {
        if (input == null) return null;
        return input.stream()
                .map(MetaTagSanitizer::sanitize)
                .collect(Collectors.toList());
    }

    private static String sanitize(String s) {
        if (s == null) return null;

        s = s.trim();
        s = normalizeNewLines(s);
        s = StringEscapeUtils.unescapeHtml(s);
        s = StringEscapeUtils.unescapeJava(s);

        return s;
    }

    private static String normalizeNewLines(String s) {
        s = s.replaceAll("[\\n\\r]", " ");
        s = s.replaceAll("[ ]{2,}", " ");
        return s;
    }
}
