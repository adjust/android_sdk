package com.adjust.sdk.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class MockHttpClient implements HttpClient {

    private MockLogger testLogger;
    private String prefix = "HttpClient ";
    private String messageError;
    private String responseError;

    public MockHttpClient(MockLogger testLogger) {
        this.testLogger = testLogger;
        messageError = null;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException,
            ClientProtocolException {
        testLogger.test(prefix +  "execute HttpUriRequest request");

        if (messageError != null) {
            throw new ClientProtocolException(messageError);
        }

        if (responseError != null)
            return getMockResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "{ \"error\": \"" + responseError + "\"}");
        else
            return getMockResponse(HttpStatus.SC_OK, "{ \"tracker_token\": \"token\", \"tracker_name\": \"name\", \"network\": \"network\", \"campaign\": \"campaign\", \"adgroup\": \"adgroup\", \"creative\": \"creative\"}");
    }

    private HttpResponse getMockResponse(int statusCode, String responseData) throws IOException {
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, null);
        HttpResponse response = new BasicHttpResponse(statusLine);
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inStream = new ByteArrayInputStream(responseData.getBytes("UTF-8"));

        entity.setContent(inStream);
        response.setEntity(entity);
        return response;
    }

    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }

    public void setResponseError(String responseError) {
        this.responseError = responseError;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest arg0, ResponseHandler<? extends T> arg1)
            throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request,
            HttpContext context) throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest arg0,
            ResponseHandler<? extends T> arg1, HttpContext arg2)
            throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpHost arg0, HttpRequest arg1,
            ResponseHandler<? extends T> arg2) throws IOException,
            ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpHost arg0, HttpRequest arg1,
            ResponseHandler<? extends T> arg2, HttpContext arg3)
            throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

}
