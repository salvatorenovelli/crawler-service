package com.myseotoolbox.crawler.utils;

import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.*;

public class UriUtilsTest {

    @Test
    public void shouldGetFolderWhenUrlHasFolderAndFile() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/file");
        assertEquals("/folder/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlHasNoFolder() throws Exception {
        String result = UriUtils.getFolder("http://example.com/file");
        assertEquals("/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlIsRootOnly() throws Exception {
        String result = UriUtils.getFolder("http://example.com/");
        assertEquals("/", result);
    }

    @Test
    public void shouldReturnRootWhenUrlHasEmptyPath() throws Exception {
        String result = UriUtils.getFolder("http://example.com");
        assertEquals("/", result);
    }

    @Test
    public void shouldExtractNestedFoldersFromUrl() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/subfolder/file");
        assertEquals("/folder/subfolder/", result);
    }

    @Test
    public void shouldIgnoreQueryParamsAndGetFolder() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder/file?query=value");
        assertEquals("/folder/", result);
    }

    @Test
    public void shouldGetFolderWhenUrlHasEncodedCharacters() throws Exception {
        String result = UriUtils.getFolder("http://example.com/folder%20name/file");
        assertEquals("/folder%20name/", result);
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsInvalid() {
        assertThrows(MalformedURLException.class, () -> UriUtils.getFolder("invalid-url"));
    }

    @Test
    public void shouldThrowExceptionWhenUrlIsMissingSchema() {
        assertThrows(MalformedURLException.class, () -> UriUtils.getFolder("example.com/folder/file"));
    }
}
