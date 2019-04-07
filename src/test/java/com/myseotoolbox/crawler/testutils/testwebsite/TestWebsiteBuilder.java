package com.myseotoolbox.crawler.testutils.testwebsite;

import com.myseotoolbox.crawler.httpclient.SafeStringEscaper;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestWebsiteBuilder {

    private final Server server;
    private Page curPage = null;
    private Map<String, Page> pages = new HashMap<>();

    static {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "INFO");
    }

    private TestWebsiteRequestHandler handler;

    InputStream robotsTxtStream;
    boolean robotsTxtRedirect = false;

    private TestWebsiteBuilder(Server server) {
        this.server = server;
    }

    public static TestWebsiteBuilder build() {
        return new TestWebsiteBuilder(new Server(0));
    }

    public TestWebsiteBuilder havingRootPage() {
        curPage = new Page("/");
        return this;
    }

    public TestWebsiteBuilder havingPage(String pagePath) {
        if (curPage != null) throw new IllegalStateException("You didn't save the previous stubbed page!");
        curPage = new Page(pagePath);
        return this;
    }

    public TestWebsiteBuilder redirectingTo(int status, String dstUri) {
        curPage.setAsRedirect(status, dstUri);
        return this;
    }

    public TestWebsiteBuilder withTitle(String s) {
        curPage.setTitle(s);
        return this;
    }

    public TestWebsiteBuilder withTag(String tagName, String content) {
        curPage.addTag(tagName, content);
        return this;
    }

    public TestWebsiteBuilder withMetaDescription(String s) {
        curPage.setMetaDescription(s);
        return this;
    }

    public TestWebsiteBuilder withMimeType(String mimeType) {
        this.curPage.setMimeType(mimeType);
        return this;
    }

    public TestWebsiteBuilder withLinksTo(String... links) {
        this.curPage.addLinks(links);
        return this;
    }

    public TestWebsiteBuilder and() {
        if (curPage != null) {
            addPage(curPage);
            curPage = null;
        }
        return this;
    }

    public URI buildTestUri(String uri) {
        if (!server.isStarted()) {
            throw new IllegalStateException("Sorry, you'll need to start the server before asking for URI. (At the moment the server port is not known)");
        }
        int localPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        return URI.create("http://localhost:" + localPort + uri);
    }

    public TestWebsite save() {
        if (curPage != null) {
            addPage(curPage);
        }
        return handler;
    }

    public TestWebsite run() throws Exception {

        and();
        handler = new TestWebsiteRequestHandler(this);
        server.setHandler(handler);
        server.start();
        log.info("Test server listening on http://localhost:{}", ((ServerConnector) server.getConnectors()[0]).getLocalPort());

        return handler;
    }

    public void tearDown() throws Exception {
        server.stop();
    }

    Page getPage(String s) {
        return pages.get(encode(s));
    }

    private String encode(String s) {
        return SafeStringEscaper.escapeString(s);
    }

    private void addPage(Page page) {
        pages.putIfAbsent(encode(page.getPagePath()), page);
    }

    public TestWebsiteBuilder disableCharsetHeader() {
        curPage.setCharsetFieldPresent(false);
        return this;
    }

    public TestWebsiteBuilder withRobotsTxt(InputStream stream) {
        this.robotsTxtStream = stream;
        return this;
    }

    public TestWebsiteBuilder withRobotTxtHavingRedirect() {
        this.robotsTxtRedirect = true;
        return this;
    }
}

