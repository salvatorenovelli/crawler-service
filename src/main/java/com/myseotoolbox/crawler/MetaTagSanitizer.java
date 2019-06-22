package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;

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

        s = html2text(s);
        s = StringEscapeUtils.unescapeHtml(s);
        s = StringEscapeUtils.unescapeJava(s);
        s = normalizeNewLines(s);
        s = removeMultipleSpaces(s);
        s = s.trim();

        return s;
    }

    private static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    private static String normalizeNewLines(String s) {
        s = s.replaceAll("[\\n\\r]", " ");
        return s;
    }

    private static String removeMultipleSpaces(String s) {
        //Normalize &nbsp; to normal space 0x20. (unescapeHtml would translated &nbsp; into  0xA0 otherwise)
        s = s.replace("\u00a0", " ");
        s = s.replaceAll("[ ]{2,}", " ");
        return s;
    }
}
