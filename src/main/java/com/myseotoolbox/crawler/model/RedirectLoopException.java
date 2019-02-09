package com.myseotoolbox.crawler.model;

public class RedirectLoopException extends Exception {
    public RedirectLoopException() {
        super("ERROR: Redirect Loop");
    }
}
