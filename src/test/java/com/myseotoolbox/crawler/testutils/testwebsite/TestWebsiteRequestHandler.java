package com.myseotoolbox.crawler.testutils.testwebsite;

import com.myseotoolbox.crawler.testutils.TestWebsite;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;

@Slf4j
class TestWebsiteRequestHandler extends AbstractHandler implements TestWebsite {
    private TestWebsiteBuilder testWebsiteBuilder;
    private List<ReceivedRequest> requestsReceived = new ArrayList<>();

    public TestWebsiteRequestHandler(TestWebsiteBuilder testWebsiteBuilder) {
        this.testWebsiteBuilder = testWebsiteBuilder;
    }

    @Override
    public void handle(String s, Request request,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) {


        requestsReceived.add(ReceivedRequest.from(request));
        log.info("Received request for path '{}'. {}", s, request);
        Page page = testWebsiteBuilder.getPage(s);
        if (page != null) {
            servePage(page, httpServletResponse);
            request.setHandled(true);
        }


    }

    private void servePage(Page page, HttpServletResponse response) {
        int status = page.getStatus();

        if (status == 200) {
            serveStandard(page, response);
        } else if (isRedirect(status)) {
            serveAsRedirect(page, response);
        } else {
            throw new UnsupportedOperationException("Status not supported" + status);
        }


    }

    private void serveAsRedirect(Page page, HttpServletResponse response) {
        response.setStatus(page.getStatus());
        response.setHeader("location", page.getRedirectUri());
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

        for (String tag : page.getTags().keySet()) {
            for (String content : page.getTags().get(tag)) {
                sb.append("        <").append(tag).append(">").append(content).append("</").append(tag).append(">");
            }
        }


    }

    @Override
    public List<ReceivedRequest> getRequestsReceived() {
        return requestsReceived;
    }
}
