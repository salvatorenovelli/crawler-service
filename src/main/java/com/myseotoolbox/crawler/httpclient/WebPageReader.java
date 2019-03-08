package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.CalendarService;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChain;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.model.RedirectLoopException;
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
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.*;

@Slf4j
@Component
public class WebPageReader {

    private final HtmlParser parser = new HtmlParser();
    private final CalendarService calendarService = new CalendarService();

    public PageSnapshot snapshotPage(URI uri) throws SnapshotException {

        String startURI = uri.toString();
        RedirectChain chain = new RedirectChain();

        try {

            URI baseUri = buildUri(startURI);
            scanRedirectChain(chain, baseUri);

            PageSnapshot snapshot = parser.parse(startURI, chain.getElements(), chain.getInputStream());
            snapshot.setCreateDate(calendarService.now());

            return snapshot;

        } catch (Exception e) {
            PageSnapshot pageSnapshot = new PageSnapshot();
            pageSnapshot.setUri(startURI);
            pageSnapshot.setCreateDate(calendarService.now());
            pageSnapshot.setRedirectChainElements(chain.getElements());
            pageSnapshot.setCrawlStatus("Unable to crawl: " + e.toString());
            throw new SnapshotException(e, pageSnapshot);
        }

    }

    public static boolean isRedirect(int statusCode) {
        switch (statusCode) {
            case SC_MOVED_PERMANENTLY: // 301
            case SC_MOVED_TEMPORARILY: // 302
            case SC_SEE_OTHER: // 303
            case SC_TEMPORARY_REDIRECT: // 307
            case 308:
                return true;
            default:
                return false;
        }
    }

    private void scanRedirectChain(RedirectChain redirectChain, URI currentURI) throws IOException, URISyntaxException, RedirectLoopException {

        HttpResponse response = new HttpGetRequest(currentURI).execute();

        int httpStatus = response.getHttpStatus();
        URI location = response.getLocation();

        redirectChain.addElement(new RedirectChainElement(decode(currentURI), httpStatus, decode(location)));

        if (isRedirect(httpStatus)) {
            scanRedirectChain(redirectChain, location);
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

    private URI buildUri(String startURI) throws URISyntaxException {

        if (!startURI.startsWith("http")) {
            startURI = "http://" + startURI;
        }

        return new URI(startURI.trim());
    }

}