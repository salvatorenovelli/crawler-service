package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HtmlParserTest {

    @Test
    public void canFindTitleOutsideHead() throws IOException {
        InputStream is = toInputStream("<HTML><BODY><TITLE>This is the title</TITLE></BODY></HTML>", StandardCharsets.UTF_8);
        HtmlParser sut = new HtmlParser();
        PageSnapshot parse = sut.parse("http://somehost", Collections.emptyList(), is);
        assertThat(parse.getTitle(), is("This is the title"));
    }

    @Test
    public void shouldSanitizeTags() throws IOException {
        InputStream is = toInputStream("<HTML>" +
                "<BODY>" +
                "<h1><p>this contains a paragraph</p></h1>" +
                "<h2>this contains a <b>bold</b></h1>" +
                "</BODY></HTML>", StandardCharsets.UTF_8);


        HtmlParser sut = new HtmlParser();
        PageSnapshot parse = sut.parse("http://somehost", Collections.emptyList(), is);
        assertThat(parse.getH1s().get(0), is("this contains a paragraph"));
        assertThat(parse.getH2s().get(0), is("this contains a bold"));
    }
}