package com.myseotoolbox.crawl;


import com.myseotoolbox.crawl.model.RedirectChainElement;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class StandardMetaTagValues {
    public static final Date STANDARD_DATE = new Date();
    public static final String STANDARD_URI = "http://uri";
    public static final String STANDARD_TITLE = "Title";
    public static final List<String> STANDARD_META_DESCR = asList("Meta1", "Meta2");
    public static final List<String> STANDARD_H1 = asList("H1-1", "H1-2");
    public static final List<String> STANDARD_H2 = asList("H2-1", "H2-2");
    public static final List<String> STANDARD_CANONICAL = asList("Canonical1", "Canonical2");
    public static final List<RedirectChainElement> STANDARD_REDIRECT_CHAIN_ELEMENTS = singletonList(new RedirectChainElement(STANDARD_URI, 200, STANDARD_URI));

}
