package com.adjust.sdk.test;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

public class MockStatusLine implements StatusLine {

    public MockStatusLine() {
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.SC_OK;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public String getReasonPhrase() {
        return null;
    }

}
