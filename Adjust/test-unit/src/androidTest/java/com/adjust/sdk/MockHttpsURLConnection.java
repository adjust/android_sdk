package com.adjust.sdk;

import android.os.SystemClock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by pfms on 20/08/15.
 */
public class MockHttpsURLConnection extends HttpsURLConnection {

    private MockLogger testLogger;
    private String prefix = "MockHttpsURLConnection ";
    private ByteArrayOutputStream outputStream;
    public ResponseType responseType;
    public boolean timeout;
    public Long waitingTime;

    protected MockHttpsURLConnection(URL url) {
        super(url);
    }

    public MockHttpsURLConnection(URL url, MockLogger mockLogger) {
        this(url);
        this.testLogger = mockLogger;
    }

    public InputStream getInputStream() throws IOException {
        testLogger.test(prefix + "getInputStream, responseType: " + responseType);

        if (timeout) {
            SystemClock.sleep(10000);
        }

        if (waitingTime != null) {
            SystemClock.sleep(waitingTime);
        }

        if (responseType == ResponseType.CLIENT_PROTOCOL_EXCEPTION) {
            throw new IOException ("testResponseError");
        } else if (responseType == ResponseType.WRONG_JSON) {
            return getMockResponse("not a json response");
        } else if (responseType == ResponseType.EMPTY_JSON) {
            return getMockResponse("{ }");
        } else if (responseType == ResponseType.MESSAGE) {
            return getMockResponse("{ \"message\" : \"response OK\"}");
        }
        return null;
    }

    public InputStream getErrorStream() {
        testLogger.test(prefix + "getErrorStream, responseType: " + responseType);
        try {
            if (responseType == ResponseType.INTERNAL_SERVER_ERROR) {
                return getMockResponse("{ \"message\": \"testResponseError\"}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getMockResponse(String response)
            throws IOException {

        InputStream stream = new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8")));

        return stream;
    }


    public String readRequest() {
        String out = null;
        try {
            out = new String(outputStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            testLogger.test("readRequest, UnsupportedEncodingException " + e.getMessage());
        }
        return out;
    }

    @Override
    public void disconnect() {
        testLogger.test(prefix + "disconnect");
    }

    @Override
    public boolean usingProxy() {
        testLogger.test(prefix + "usingProxy");
        return false;
    }

    @Override
    public void connect() throws IOException {
        testLogger.test(prefix + "connect");
    }

    public Permission getPermission() throws IOException {
        testLogger.test(prefix + "getPermission");
        return null;
    }

    public String getRequestMethod() {
        testLogger.test(prefix + "getRequestMethod");
        return null;
    }

    public int getResponseCode() throws IOException {
        testLogger.test(prefix + "getResponseCode");
        if (responseType == ResponseType.INTERNAL_SERVER_ERROR) {
            return HttpsURLConnection.HTTP_INTERNAL_ERROR;
        } else {
            return HttpsURLConnection.HTTP_OK;
        }
    }

    public String getResponseMessage() throws IOException {
        testLogger.test(prefix + "getResponseMessage");
        return null;
    }

    public void setRequestMethod(String method) throws ProtocolException {
        testLogger.test(prefix + "setRequestMethod, method " + method);
        super.setRequestMethod(method);
    }

    public String getContentEncoding() {
        testLogger.test(prefix + "getContentEncoding");
        return null;
    }

    public boolean getInstanceFollowRedirects() {
        testLogger.test(prefix + "getInstanceFollowRedirects");
        return false;
    }

    public void setInstanceFollowRedirects(boolean followRedirects) {
        testLogger.test(prefix + "setInstanceFollowRedirects, followRedirects " + followRedirects);
    }

    public long getHeaderFieldDate(String field, long defaultValue) {
        testLogger.test(prefix + "getHeaderFieldDate, field " + field + ", defaultValue " + defaultValue);
        return 0;
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        testLogger.test(prefix + "setFixedLengthStreamingMode, contentLength " + contentLength);
    }

    public void setFixedLengthStreamingMode(int contentLength) {
        testLogger.test(prefix + "setFixedLengthStreamingMode, contentLength " + contentLength);
    }

    public void setChunkedStreamingMode(int chunkLength) {
        testLogger.test(prefix + "setChunkedStreamingMode, chunkLength " + chunkLength);
    }

    public boolean getAllowUserInteraction() {
        testLogger.test(prefix + "getAllowUserInteraction");
        return false;
    }

    public Object getContent() throws IOException {
        testLogger.test(prefix + "getReferrer");
        return null;
    }

    public Object getContent(Class[] types) throws IOException {
        testLogger.test(prefix + "getReferrer, types " + types);
        return null;
    }

    public int getContentLength() {
        testLogger.test(prefix + "getContentLength");
        return 0;
    }

    public String getContentType() {
        testLogger.test(prefix + "getContentLength");
        return null;
    }

    public long getDate() {
        testLogger.test(prefix + "getDate");
        return 0;
    }

    public boolean getDefaultUseCaches() {
        testLogger.test(prefix + "getDefaultUseCaches");
        return false;
    }

    public boolean getDoInput() {
        testLogger.test(prefix + "getDoInput");
        return false;
    }

    public boolean getDoOutput() {
        testLogger.test(prefix + "getDoOutput");
        return false;
    }

    public long getExpiration() {
        testLogger.test(prefix + "getExpiration");
        return 0;
    }

    public String getHeaderField(int pos) {
        testLogger.test(prefix + "getHeaderField, pos " + pos);
        return null;
    }

    public Map<String, List<String>> getHeaderFields() {
        testLogger.test(prefix + "getHeaderFields");
        return null;
    }

    public Map<String, List<String>> getRequestProperties() {
        testLogger.test(prefix + "getRequestProperties");
        return null;
    }

    public void addRequestProperty(String field, String newValue) {
        testLogger.test(prefix + "addRequestProperty, field " + field + ", newValue " + newValue);
    }

    public String getHeaderField(String key) {
        testLogger.test(prefix + "getHeaderField, key " + key);
        return null;
    }

    public int getHeaderFieldInt(String field, int defaultValue) {
        testLogger.test(prefix + "getHeaderFieldInt, field " + field + ", defaultValue " + defaultValue);
        return 0;
    }

    public String getHeaderFieldKey(int posn) {
        testLogger.test(prefix + "getHeaderFieldKey, " + posn);
        return null;
    }

    public long getIfModifiedSince() {
        testLogger.test(prefix + "getIfModifiedSince");
        return 0;
    }

    public long getLastModified() {
        testLogger.test(prefix + "getLastModified");
        return 0;
    }

    public OutputStream getOutputStream() throws IOException {
        testLogger.test(prefix + "getOutputStream");
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    public String getRequestProperty(String field) {
        testLogger.test(prefix + "getRequestProperty, field " + field);
        return null;
    }

    public URL getURL() {
        testLogger.test(prefix + "getURL");
        return this.url;
    }

    public void setURL(URL url) {
        testLogger.test(prefix + "setURL, " + url);

    }

    public boolean getUseCaches() {
        testLogger.test(prefix + "getUseCaches");
        return false;
    }

    public void setAllowUserInteraction(boolean newValue) {
        testLogger.test(prefix + "setAllowUserInteraction, newValue " + newValue);
    }

    public void setDefaultUseCaches(boolean newValue) {
        testLogger.test(prefix + "setDefaultUseCaches, newValue " + newValue);
    }

    public void setDoInput(boolean newValue) {
        testLogger.test(prefix + "setDoInput, newValue " + newValue);
        super.setDoInput(newValue);
    }

    public void setDoOutput(boolean newValue) {
        testLogger.test(prefix + "setDoOutput, newValue " + newValue);
        super.setDoOutput(newValue);
    }

    public void setIfModifiedSince(long newValue) {
        testLogger.test(prefix + "setIfModifiedSince, newValue " + newValue);
    }

    public void setRequestProperty(String field, String newValue) {
        testLogger.test(prefix + "setRequestProperty, field " + field + ", newValue " + newValue);
        super.setRequestProperty(field, newValue);
    }

    public void setUseCaches(boolean newValue) {
        testLogger.test(prefix + "setUseCaches, newValue " + newValue);
        super.setUseCaches(newValue);
    }

    public void setConnectTimeout(int timeoutMillis) {
        testLogger.test(prefix + "setConnectTimeout, timeoutMillis " + timeoutMillis);
        super.setConnectTimeout(timeoutMillis);
    }

    public int getConnectTimeout() {
        testLogger.test(prefix + "getConnectTimeout");
        return 0;
    }

    public void setReadTimeout(int timeoutMillis) {
        testLogger.test(prefix + "setReadTimeout, timeoutMillis " + timeoutMillis);
    }

    public int getReadTimeout() {
        testLogger.test(prefix + "getReadTimeout");
        return 0;
    }

    public String toString() {
        testLogger.test(prefix + "toString");
        return null;
    }

    @Override
    public String getCipherSuite() {
        testLogger.test(prefix + "getCipherSuite");
        return null;
    }

    @Override
    public Certificate[] getLocalCertificates() {
        testLogger.test(prefix + "getLocalCertificates");
        return new Certificate[0];
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        testLogger.test(prefix + "getServerCertificates");
        return new Certificate[0];
    }
}
