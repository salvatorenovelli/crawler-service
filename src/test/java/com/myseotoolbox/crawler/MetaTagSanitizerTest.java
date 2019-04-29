package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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
}