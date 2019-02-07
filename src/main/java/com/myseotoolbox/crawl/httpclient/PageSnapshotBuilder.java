package com.myseotoolbox.crawl.httpclient;

import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.model.RedirectChainElement;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


class PageSnapshotBuilder {

    static PageSnapshot build(String uri, List<RedirectChainElement> elements, Document page) {

        PageSnapshot pageSnapshot = new PageSnapshot(
                removeFragment(uri),
                page.title(),
                getTagContents(page, "H1"),
                getTagContents(page, "H2"),
                extractFromTag(page, "meta[name=\"description\"]", element -> element.attr("content")),
                extractFromTag(page, "link[rel=\"canonical\"]", element -> element.attr("href")));

        pageSnapshot.setRedirectChainElements(elements);
        return pageSnapshot;
    }

    private static String removeFragment(String uri) {
        return uri.split("#")[0];
    }

    private static List<String> getTagContents(Element page, String tag) {
        return extractFromTag(page, tag, Element::html);
    }

    private static List<String> extractFromTag(Element element, String filter, Function<Element, String> mapper) {
        return element
                .select(filter).stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}