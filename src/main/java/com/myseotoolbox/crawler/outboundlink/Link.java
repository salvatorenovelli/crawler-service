package com.myseotoolbox.crawler.outboundlink;

import lombok.Data;

@Data
public class Link {
    enum Type {AHREF, CANONICAL}

    private final Type type;
    private final String url;

}
