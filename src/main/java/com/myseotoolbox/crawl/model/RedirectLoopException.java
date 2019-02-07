package com.myseotoolbox.crawl.model;

public class RedirectLoopException extends Exception {
    public RedirectLoopException() {
        super("ERROR: Redirect Loop");
    }
}
