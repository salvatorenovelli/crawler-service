package com.myseotoolbox.crawl.httpclient;

import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.model.RedirectChain;
import com.myseotoolbox.crawl.model.RedirectChainElement;
import com.myseotoolbox.crawl.model.RedirectLoopException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.*;

@Slf4j
@Component
public class WebPageReader {

    private final HtmlParser parser = new HtmlParser();

    public PageSnapshot snapshotPage(URI uri) throws SnapshotException{
        try {
            return analyseRedirectChain(uri.toString());
        } catch (Exception e) {
            throw new SnapshotException(e);
        }
    }

    public static boolean isRedirect(int statusCode) {
        switch (statusCode) {
            case SC_MOVED_PERMANENTLY: // 301
            case SC_MOVED_TEMPORARILY: // 302
            case SC_SEE_OTHER: // 303
            case SC_TEMPORARY_REDIRECT: // 307
                return true;
            default:
                return false;
        }
    }

    private PageSnapshot analyseRedirectChain(String startURI) {

        RedirectChain chain = new RedirectChain();

        try {

            URI baseUri = buildUri(startURI);
            scanChain(chain, baseUri);

            return parser.parse(startURI, chain.getElements(), chain.getInputStream());

        } catch (Exception e) {
            PageSnapshot pageSnapshot = new PageSnapshot();
            pageSnapshot.setUri(startURI);
            pageSnapshot.setRedirectChainElements(chain.getElements());
            pageSnapshot.setCrawlStatus("Unable to crawl: " + e.toString());
            return pageSnapshot;
        }

    }

    private void scanChain(RedirectChain redirectChain, URI currentURI) throws IOException, URISyntaxException, RedirectLoopException {

        HttpResponse response = new HttpGetRequest(currentURI).execute();

        int httpStatus = response.getHttpStatus();
        URI location = response.getLocation();

        redirectChain.addElement(new RedirectChainElement(decode(currentURI), httpStatus, decode(location)));

        if (isRedirect(httpStatus)) {
            scanChain(redirectChain, location);
        } else {
            redirectChain.setInputStream(response.getInputStream());
        }

    }

    private String decode(URI currentURI) {
        try {
            return URLDecoder.decode(currentURI.toASCIIString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Document toJsoupDocument(InputStream inputStream, String baseUri) throws IOException {
        return Jsoup.parse(inputStream, UTF_8.name(), baseUri);
    }

    private URI buildUri(String startURI) throws URISyntaxException {

        if (!startURI.startsWith("http")) {
            startURI = "http://" + startURI;
        }

        return new URI(startURI.trim());
    }

}