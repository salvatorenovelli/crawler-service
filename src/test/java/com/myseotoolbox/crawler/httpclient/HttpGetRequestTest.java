package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpGetRequestTest {

    public static final String LOCATION_WITH_UNICODE_CHARACTERS = "/família";

    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }


    @Test
    public void itShouldBeAbleToDealWithDifferentCharsets() throws Exception {

        String basePath = "/тэг/уход-за-одеждой";

        givenAWebsite()
                .havingPage(basePath)
                .redirectingTo(301, "/teg/Ñ\u0083Ñ\u0085Ð¾Ð´-Ð·Ð°-Ð¾Ð´ÐµÐ¶Ð´Ð¾Ð¹").save();


        HttpResponse execute = new HttpGetRequest(testUri(basePath)).execute();
        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_MOVED_PERMANENTLY));
        assertThat(execute.getLocation(), is(testUri("/teg/%D1%83%D1%85%D0%BE%D0%B4-%D0%B7%D0%B0-%D0%BE%D0%B4%D0%B5%D0%B6%D0%B4%D0%BE%D0%B9")));
    }

    @Test
    public void usesGetOn301WithRelativeDstPath() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(301, "/relative_destination").save();

        HttpResponse execute = new HttpGetRequest(testUri("/source")).execute();

        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_MOVED_PERMANENTLY));
        assertThat(execute.getLocation(), is(testUri("/relative_destination")));
    }

    @Test
    public void usesGetOn301WithAbsoluteDstPath() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(301, "http://absolute_destination").save();


        HttpResponse execute = new HttpGetRequest(testUri("/source")).execute();

        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_MOVED_PERMANENTLY));
        assertThat(execute.getLocation(), is(URI.create("http://absolute_destination")));
    }

    @Test
    public void testGetOn302Redirect() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(302, "/destination").save();

        HttpResponse execute = new HttpGetRequest(testUri("/source")).execute();

        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_MOVED_TEMPORARILY));
        assertThat(execute.getLocation(), is(testUri("/destination")));
    }

    @Test
    public void testGetOn303SeeOther() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(303, "/destination").save();

        HttpResponse execute = new HttpGetRequest(testUri("/source")).execute();

        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_SEE_OTHER));
        assertThat(execute.getLocation(), is(testUri("/destination")));
    }

    @Test
    public void getOn200() throws Exception {
        givenAWebsite()
                .havingPage("/hello").save();

        HttpResponse execute = new HttpGetRequest(testUri("/hello")).execute();

        assertThat(execute.getHttpStatus(), is(HttpStatus.SC_OK));
        assertThat(execute.getLocation(), is(testUri("/hello")));
    }

    /**
     * Some website, put unicode characters in their target location (without escaping them).
     * <p>
     * The response bytes containing the unicode characters will end up being "decoded" into string with the default charset, and returned as a
     * header parameter. The problem is that the web server might have encoded them in a different charset than ours,
     * therefore we had to read the charset from the header and transcode the string into the intended charset.
     * <p>
     * <p>
     * PS: I used two redirects (firstPass, secondPass) as it made the test easier as we didn't have to assert against a unicode target URL.
     */
    @Test
    public void weShouldHandleUnicodeCharacters() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(301, LOCATION_WITH_UNICODE_CHARACTERS).and()
                .havingPage(LOCATION_WITH_UNICODE_CHARACTERS).redirectingTo(301, "/destination").save();


        HttpResponse firstPass = new HttpGetRequest(testUri("/source")).execute();
        HttpResponse secondPass = new HttpGetRequest(firstPass.getLocation()).execute();

        assertThat(secondPass.getHttpStatus(), is(HttpStatus.SC_MOVED_PERMANENTLY));
        assertThat(secondPass.getLocation(), is(testUri("/destination")));
    }

    @Test
    public void unicodeRedirectGetUrlEncoded() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(301, LOCATION_WITH_UNICODE_CHARACTERS).save();

        HttpResponse request = new HttpGetRequest(testUri("/source")).execute();
        assertThat(request.getLocation(), is(testUri("/fam%edlia")));

    }

    @Test
    public void unicodeCharactersInLocationHeaderGetDecodedEvenWhenCharsetIsNotSpecified() throws Exception {
        givenAWebsite()
                .havingPage("/source").redirectingTo(301, LOCATION_WITH_UNICODE_CHARACTERS)
                .disableCharsetHeader()
                .save();

        HttpResponse request = new HttpGetRequest(testUri("/source")).execute();
        assertThat(request.getLocation(), is(testUri("/fam%edlia")));

    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }
}
