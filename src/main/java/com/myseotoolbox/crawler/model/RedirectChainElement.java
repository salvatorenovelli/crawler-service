package com.myseotoolbox.crawler.model;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data@NoArgsConstructor
public class RedirectChainElement {

    private int httpStatus;
    private String sourceURI;
    private String destinationURI;

    public RedirectChainElement(String sourceUri, int httpStatus, String destinationURI) {
        this.httpStatus = httpStatus;
        this.sourceURI = sourceUri;
        this.destinationURI = destinationURI;
    }
}
