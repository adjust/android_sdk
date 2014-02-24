package com.adjust.sdk.test;

import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

public class MockHttpResponse implements HttpResponse {

    public MockHttpResponse() {
    }


    @Override
    public StatusLine getStatusLine() {
        return new MockStatusLine();
    }

    @Override
    public HttpEntity getEntity() {
        return new MockHttpEntity();
    }

    @Override
    public void addHeader(Header arg0) {

    }

    @Override
    public void addHeader(String arg0, String arg1) {

    }

    @Override
    public boolean containsHeader(String arg0) {
        return false;
    }

    @Override
    public Header[] getAllHeaders() {
        return null;
    }

    @Override
    public Header getFirstHeader(String arg0) {
        return null;
    }

    @Override
    public Header[] getHeaders(String arg0) {
        return null;
    }

    @Override
    public Header getLastHeader(String arg0) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String arg0) {
        return null;
    }

    @Override
    public void removeHeader(Header arg0) {

    }

    @Override
    public void removeHeaders(String arg0) {

    }

    @Override
    public void setHeader(Header arg0) {

    }

    @Override
    public void setHeader(String arg0, String arg1) {

    }

    @Override
    public void setHeaders(Header[] arg0) {

    }

    @Override
    public void setParams(HttpParams arg0) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setEntity(HttpEntity entity) {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {

    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {

    }

    @Override
    public void setStatusLine(StatusLine statusline) {

    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {

    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {

    }

}
