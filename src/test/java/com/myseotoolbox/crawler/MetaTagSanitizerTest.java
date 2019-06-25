package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MetaTagSanitizerTest {

    @Test
    public void shouldSanitizeHtmlCharacters() {

        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("This contains &lt; html characters");

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), is("This contains < html characters"));
    }

    @Test
    public void normalizeSpace() {
        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("This is&nbsp;a type of space");

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), is("This is a type of space"));
    }

    @Test
    public void shouldRemoveMultipleSpaces() {

        //Removing spaces on the basis that even if they will be visible to users, it's unlikely that they will be used as recommendation
        //therefore if a live title has an additional space it should match a recommendation with a single space
        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("This contains title is intended to have &nbsp; multiple spaces");

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), is("This contains title is intended to have multiple spaces"));
    }

    @Test
    public void shouldTrimNbsps() {
        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("This title has deliberate nbsp at the end&nbsp;");

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), is("This title has deliberate nbsp at the end"));
    }

    @Test
    public void shouldStripHtml() {
        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("This title has <b>bold</b>");

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), is("This title has bold"));
    }

    @Test
    public void sanitizerTrimsValues() {

        PageSnapshot snapshot = new PageSnapshot();
        snapshot.setTitle("  title  ");
        snapshot.setH1s(Arrays.asList(" h1-1 ", " h1-2 "));
        snapshot.setH2s(Arrays.asList(" h2 "));
        snapshot.setMetaDescriptions(Arrays.asList(" meta "));
        snapshot.setCanonicals(Arrays.asList(" canonicals "));

        MetaTagSanitizer.sanitize(snapshot);

        assertThat(snapshot.getTitle(), Matchers.is("title"));
        assertThat(snapshot.getH1s().get(0), Matchers.is("h1-1"));
        assertThat(snapshot.getH1s().get(1), Matchers.is("h1-2"));
        assertThat(snapshot.getH2s().get(0), Matchers.is("h2"));
        assertThat(snapshot.getMetaDescriptions().get(0), Matchers.is("meta"));

    }


    @Test
    public void lineBreakAreRemoved() {


        PageSnapshot e = new PageSnapshot(
                "http://someuri",
                "title\rwith Mac break",
                asList("h1-\nwith correct line break", "h1-\r\nwith windows break"),
                singletonList("h2\rwith break"),
                singletonList("meta\rwith break"),
                singletonList("canonical"));

        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("title with Mac break"));
        assertThat(e.getH1s().get(0), Matchers.is("h1- with correct line break"));
        assertThat(e.getH1s().get(1), Matchers.is("h1- with windows break"));
        assertThat(e.getH2s().get(0), Matchers.is("h2 with break"));
        assertThat(e.getMetaDescriptions().get(0), Matchers.is("meta with break"));

    }


    @Test
    public void multipleSpacesAreRemoved() {

        PageSnapshot e = new PageSnapshot(
                "http://someuri",
                "title   multiple spaces   ",
                asList("multiple spaces  here too  ", "  some spaces                     here too  "),
                singletonList("multiple spaces  here too  "),
                singletonList("multiple spaces   here too  "),
                singletonList("nothing to do here..."));


        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("title multiple spaces"));
        assertThat(e.getH1s().get(0), Matchers.is("multiple spaces here too"));
        assertThat(e.getH1s().get(1), Matchers.is("some spaces here too"));
        assertThat(e.getH2s().get(0), Matchers.is("multiple spaces here too"));
        assertThat(e.getMetaDescriptions().get(0), Matchers.is("multiple spaces here too"));

    }


    @Test
    public void sanitizerRemoveNewLines() {


        PageSnapshot e = new PageSnapshot(
                "http://someuri",
                "title\rwith mac break",
                asList("h1-\nwith correct line break", "h1-\r\nwith windows break"),
                singletonList("h2\rwith break"),
                singletonList("meta\rwith break"),
                singletonList("canonical"));

        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("title with mac break"));
        assertThat(e.getH1s().get(0), Matchers.is("h1- with correct line break"));
        assertThat(e.getH1s().get(1), Matchers.is("h1- with windows break"));
        assertThat(e.getH2s().get(0), Matchers.is("h2 with break"));
        assertThat(e.getMetaDescriptions().get(0), Matchers.is("meta with break"));

    }

    @Test
    public void htmlCharacterReferencesAreUnescaped() {

        PageSnapshot e = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://someuri")
                .withTitle("Title is ok: &amp;")
                .withH1s("H1 is ok: &quot;")
                .withH2s("H2 is ok: &lt;")
                .withMetas("&lt;&gt;&quot;&amp;&copy;&reg;&rsquo;").build();

        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("Title is ok: &"));
        assertThat(e.getH1s().get(0), Matchers.is("H1 is ok: \""));
        assertThat(e.getH2s().get(0), Matchers.is("H2 is ok: <"));
        assertThat(e.getMetaDescriptions().get(0), Matchers.is("<>\"&©®’"));
    }

    @Test
    public void unicodeEscapedCharacterAreUnescaped() {

        PageSnapshot e = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://someuri")
                .withTitle("Title is ok: &#8710;").build();

        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("Title is ok: ∆"));
    }

    @Test
    public void slashEscapedCharactersAreCharacterAreUnescaped() {

        PageSnapshot e = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://someuri")
                .withTitle("Title is ok: \\'\\\"").build();

        MetaTagSanitizer.sanitize(e);

        assertThat(e.getTitle(), Matchers.is("Title is ok: '\""));
    }
}