package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.MetaTagSanitizer;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HtmlParser {
    public PageSnapshot parse(String baseUri, List<RedirectChainElement> elements, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, UTF_8.name(), baseUri);
        allowTitleInBody(document);
        PageSnapshot snapshot = PageSnapshotBuilder.build(baseUri, elements, document);
        MetaTagSanitizer.sanitize(snapshot);
        return snapshot;
    }

    private void allowTitleInBody(Document document) {
        Element titleTag = document.selectFirst("body > title");
        if (titleTag != null) {
            titleTag.remove();
            document.head().appendChild(titleTag);
        }
    }
}
