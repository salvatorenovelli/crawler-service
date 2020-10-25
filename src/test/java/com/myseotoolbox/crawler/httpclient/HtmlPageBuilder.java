package com.myseotoolbox.crawler.httpclient;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HtmlPageBuilder {
    private final StringBuffer bodyElements = new StringBuffer();
    private final StringBuffer headElements = new StringBuffer();

    public static HtmlPageBuilder givenHtmlPage() {
        return new HtmlPageBuilder();
    }


    public InputStream build() {
        return IOUtils.toInputStream("" +
                "<HTML>" +
                "  <HEAD>" +
                headElements +
                "  </HEAD>" +
                "  <BODY>" +
                bodyElements +
                "  </BODY>" +
                "</HTML>", UTF_8);
    }

    public HtmlPageBuilder withBodyElement(String element) {
        bodyElements.append(element);
        return this;
    }

    public HtmlPageBuilder and() {
        return this;
    }

    public HtmlPageBuilder withHeadElement(String element) {
        headElements.append(element);
        return this;
    }
}
