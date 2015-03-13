package com.adjust.sdk.test;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockHttpClient implements HttpClient {

    private MockLogger testLogger;
    private String prefix = "HttpClient ";
    public ResponseType responseType;
    public HttpUriRequest lastRequest;
    public boolean timeout;

    public MockHttpClient(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException,
            ClientProtocolException {
        testLogger.test(prefix + "execute, responseType: " + responseType);
        lastRequest = request;

        if (timeout) {
            testLogger.test("timing out");

        }

        if (responseType == ResponseType.CLIENT_PROTOCOL_EXCEPTION) {
            throw new ClientProtocolException("testResponseError");
        } else if (responseType == ResponseType.INTERNAL_SERVER_ERROR) {
            return getMockResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "{ \"message\": \"testResponseError\"}");
        } else if (responseType == ResponseType.WRONG_JSON) {
            return getOkResponse("not a json response");
        } else if (responseType == ResponseType.EMPTY_JSON) {
            return getOkResponse("{ }");
        } else if (responseType == ResponseType.MESSAGE) {
            return getOkResponse("{ \"message\" : \"response OK\"}");
        } else if (responseType == ResponseType.ATTRIBUTION) {
            return getOkResponse(
                    "{ \"attribution\" : {" +
                            "\"tracker_token\" : \"ttValue\" , " +
                            "\"tracker_name\"  : \"tnValue\" , " +
                            "\"network\"       : \"nValue\" , " +
                            "\"campaign\"      : \"cpValue\" , " +
                            "\"adgroup\"       : \"aValue\" , " +
                            "\"creative\"      : \"ctValue\" } }");
        } else if (responseType == ResponseType.ASK_IN) {
            return getOkResponse("{ \"ask_in\" : 4000 }");
        }

        return null;
    }

    private HttpResponse getOkResponse(String responseData)
            throws IOException {
        return getMockResponse(HttpStatus.SC_OK, responseData);
    }

    private HttpResponse getMockResponse(int statusCode, String responseData)
            throws IOException {
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, null);
        HttpResponse response = new BasicHttpResponse(statusLine);
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inStream = new ByteArrayInputStream(responseData.getBytes("UTF-8"));

        entity.setContent(inStream);
        response.setEntity(entity);
        return response;
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
