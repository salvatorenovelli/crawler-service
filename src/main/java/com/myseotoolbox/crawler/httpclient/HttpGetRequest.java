package com.myseotoolbox.crawler.httpclient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;
import static com.myseotoolbox.crawler.spider.PageLinksHelper.toValidUri;
import static com.myseotoolbox.crawler.utils.IsRedirect.isRedirect;


public class HttpGetRequest {

    public static final String BOT_NAME = "MySeoToolboxSpider";
    public static final String BOT_VERSION = "1.0";
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; " + BOT_NAME + "/" + BOT_VERSION + ")";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    private static final Logger logger = LoggerFactory.getLogger(HttpGetRequest.class);
    private final URI uri;
    private final HttpURLConnectionFactory connectionFactory;

    public HttpGetRequest(URI uri, HttpURLConnectionFactory connectionFactory) {
        this.uri = uri;
        this.connectionFactory = connectionFactory;
    }


    public HttpResponse execute() throws IOException, URISyntaxException {

        HttpURLConnection connection = connectionFactory.createConnection(new URI(uri.toASCIIString()));

        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        logger.trace("Connecting to {}...", uri.toASCIIString());
        connection.connect();
        URI dstURI = uri;

        int status = connection.getResponseCode();

        if (isRedirect(status)) {
            dstURI = extractDestinationUri(connection, dstURI);
        }

        return new HttpResponse(status, dstURI, connection.getContentType(), status < 400 ? connection.getInputStream() : null);
    }

    private URI extractDestinationUri(HttpURLConnection connection, URI initialLocation) throws URISyntaxException {
        String locationHeader = connection.getHeaderField("location");


        if (containsUnicodeCharacters(locationHeader)) {
            logger.warn("Redirect destination {} contains non ASCII characters (as required by the standard)", connection.getURL());
            locationHeader = SafeStringEscaper.escapeString(locationHeader);
        }

        URI location = toValidUri(locationHeader)
                .orElseThrow(() -> new IllegalArgumentException("Invalid redirect destination. src: " + initialLocation + " dst: " + connection.getHeaderField("location")));

        if (location.isAbsolute()) {
            return location;
        } else {
            return initialLocation.resolve(location);
        }
    }


}