package com.myseotoolbox.crawl.testutils;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.*;


public class TestWebsiteBuilder {


    private static final Logger logger = LoggerFactory.getLogger(TestWebsiteBuilder.class);
    private static Server server;
    private Page curPage = null;
    private Map<String, Page> pages = new HashMap<>();

    private TestWebsiteBuilder(Server server) {

        TestWebsiteBuilder.server = server;
    }

    public static TestWebsiteBuilder givenAWebsite() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "INFO");
        return new TestWebsiteBuilder(new Server(0));
    }


    public static TestWebsiteBuilder givenAWebsite(String rootPagePath) {
        if (TestWebsiteBuilder.server != null) {
            throw new IllegalStateException("Please use tearDownCurrentServer() in @After/ tearDown() for test isolation.");
        }
        return new TestWebsiteBuilder(new Server(0)).havingPage(rootPagePath);
    }

    public static String getTestUriAsString(String url) {
        if (!server.isStarted()) {
            throw new IllegalStateException("Sorry, you'll need to run the scenario before asking for URI. (At the moment the server port is not known)");
        }
        int localPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        return "http://localhost:" + localPort + url;
    }


    public static URI testUri(String url) throws URISyntaxException {
        if (!server.isStarted()) {
            throw new IllegalStateException("Sorry, you'll need to run the scenario before asking for URI. (At the moment the server port is not known)");
        }
        int localPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        return new URI("http://localhost:" + localPort + url);
    }


    public static void tearDownCurrentServer() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    public TestWebsiteBuilder havingRootPage() {
        curPage = new Page("/");
        return this;
    }

    public TestWebsiteBuilder havingPage(String pagePath) {
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
        curPage.metaDescription = s;
        return this;
    }

    public TestWebsite run() throws Exception {
        if (curPage != null) {
            addPage(curPage);
        }
        Handler handler = new Handler();
        server.setHandler(handler);
        server.start();
        logger.info("Test server listening on http://localhost:{}", ((ServerConnector) server.getConnectors()[0]).getLocalPort());

        return handler;
    }

    private void addPage(Page page) {
        pages.putIfAbsent(page.getPagePath(), page);
    }

    public TestWebsiteBuilder and() {
        addPage(this.curPage);
        return this;
    }

    public TestWebsiteBuilder withMimeType(String mimeType) {
        this.curPage.setMimeType(mimeType);
        return this;
    }


    private static class Page {
        private final String pagePath;
        private int status = 200;
        private String title;
        private String metaDescription;
        private Map<String, List<String>> tags = new HashMap<>();
        private String redirectUri;
        private String mimeType = "text/html";


        public Page(String pagePath) {
            this.pagePath = pagePath;
        }

        public String getPagePath() {
            return pagePath;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }


        public void addTag(String tagName, String content) {
            this.tags.computeIfAbsent(tagName, k -> new ArrayList<>());
            this.tags.get(tagName).add(content);
        }

        public String getMetaDescription() {
            return metaDescription;
        }

        public void setAsRedirect(int status, String dstUri) {
            this.status = status;
            this.redirectUri = dstUri;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

    private class Handler extends AbstractHandler implements TestWebsite {
        private List<ReceivedRequest> requestsReceived = new ArrayList<>();

        @Override
        public void handle(String s, Request request,
                           HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {


            requestsReceived.add(ReceivedRequest.from(request));
            logger.info("Received request for path '{}'. {}", s, request);
            Page page = pages.get(s);
            if (page != null) {
                servePage(page, httpServletResponse);
                request.setHandled(true);
            }


        }

        private void servePage(Page page, HttpServletResponse response) {
            int status = page.status;

            if (status == 200) {
                serveStandard(page, response);
            } else if (isRedirect(status)) {
                serveAsRedirect(page, response);
            } else {
                throw new UnsupportedOperationException("Status not supported" + status);
            }


        }

        private void serveAsRedirect(Page page, HttpServletResponse response) {
            response.setStatus(page.status);
            response.setHeader("location", page.redirectUri);
//            response.
        }

        private void serveStandard(Page page, HttpServletResponse response) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("content-type", page.getMimeType());

            try (OutputStream outputStream = response.getOutputStream()) {
                IOUtils.write(renderPage(page), outputStream, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isRedirect(int httpStatus) {
            return httpStatus == SC_MOVED_TEMPORARILY || httpStatus == SC_MOVED_PERMANENTLY || httpStatus == SC_SEE_OTHER;
        }

        private CharSequence renderPage(Page page) {
            StringBuffer sb = new StringBuffer();
            sb.append("<HTML>");
            sb.append("    <HEAD>");
            sb.append("        <TITLE>").append(page.getTitle()).append("</TITLE>");
            sb.append("        <META name=\"description\" content=\"").append(page.getMetaDescription()).append("\" />");
            sb.append("    </HEAD>");
            sb.append("    <BODY>");
            addTags(sb, page);
            sb.append("    </BODY>");
            sb.append("</HTML>");
            return sb;

        }

        private void addTags(StringBuffer sb, Page page) {

            for (String tag : page.tags.keySet()) {
                for (String content : page.tags.get(tag)) {
                    sb.append("        <").append(tag).append(">").append(content).append("</").append(tag).append(">");
                }
            }


        }

        @Override
        public List<ReceivedRequest> getRequestsReceived() {
            return requestsReceived;
        }
    }


}

