package com.myseotoolbox.crawler.httpclient;


import org.jsoup.UnsupportedMimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static com.myseotoolbox.crawler.httpclient.SafeStringEscaper.containsUnicodeCharacters;
import static com.myseotoolbox.crawler.httpclient.WebPageReader.isRedirect;


public class HttpGetRequest {

    private static final Pattern xmlContentTypeRxp = Pattern.compile("(application|text)/\\w*\\+?xml.*");


    public static final String BOT_NAME = "MySeoToolboxSpider";
    public static final String BOT_VERSION = "1.0";
    public static final String USER_AGENT = "Mozilla/5.0 (compatible; " + BOT_NAME + "/" + BOT_VERSION + ")";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    private static final Logger logger = LoggerFactory.getLogger(HttpGetRequest.class);
    private final URI uri;

    public HttpGetRequest(URI uri) {
        this.uri = uri;
    }


    public HttpResponse execute() throws IOException, URISyntaxException {

        HttpURLConnection connection = createConnection(new URI(uri.toASCIIString()));

        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        connection.connect();
        URI dstURI = uri;

        int status = connection.getResponseCode();
        checkMimeType(connection);

        if (isRedirect(status)) {
            dstURI = extractDestinationUri(connection, dstURI);
        }


        return new HttpResponse(status, dstURI, status < 400 ? connection.getInputStream() : null);
    }

    private void checkMimeType(HttpURLConnection connection) throws UnsupportedMimeTypeException {
        String contentType = connection.getContentType();
        if (contentType != null && !contentType.startsWith("text/") && !xmlContentTypeRxp.matcher(contentType).matches())
            throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml", contentType, connection.getURL().toString());
    }

    private HttpURLConnection createConnection(URI uri) throws IOException {
        return (HttpURLConnection) uri.toURL().openConnection();
    }


    private URI extractDestinationUri(HttpURLConnection connection, URI initialLocation) throws URISyntaxException {
        String locationHeader = connection.getHeaderField("location");
        URI location;


        if (containsUnicodeCharacters(locationHeader)) {
            logger.warn("Redirect destination {} contains non ASCII characters (as required by the standard)", connection.getURL());
            location = new URI(SafeStringEscaper.escapeString(locationHeader));
        } else {
            location = new URI(locationHeader);
        }

        if (location.isAbsolute()) {
            return location;
        } else {
            return initialLocation.resolve(location);
        }
    }


}