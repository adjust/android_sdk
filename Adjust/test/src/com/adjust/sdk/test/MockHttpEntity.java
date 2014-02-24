package com.adjust.sdk.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class MockHttpEntity implements HttpEntity {

    public MockHttpEntity() {
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
    }

    @Override
    public void consumeContent() throws IOException {

    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return null;
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public Header getContentType() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
