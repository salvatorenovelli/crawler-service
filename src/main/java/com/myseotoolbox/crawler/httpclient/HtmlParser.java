package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HtmlParser {
    public PageSnapshot parse(String baseUri, List<RedirectChainElement> elements, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, UTF_8.name(), baseUri);
        return PageSnapshotBuilder.build(baseUri, elements, document);
    }
}
