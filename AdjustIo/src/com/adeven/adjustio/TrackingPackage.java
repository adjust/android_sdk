// TODO: add header comments

package com.adeven.adjustio;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

public class TrackingPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    // data
    public String path;
    public String parameters;
    public String userAgent;

    // for logging
    public String kind;
    public String successMessage;
    public String failureMessage;

    public String toString() {
        return "k:" + kind +
                " pt:" + path +
                " pr:" + parameters +
                " ua:" + userAgent +
                " sm:" + successMessage +
                " fm:" + failureMessage;
    }

    public void injectEntity(HttpPost request) throws UnsupportedEncodingException {
        if (parameters == null) {
            return;
        }

        StringEntity entity = new StringEntity(parameters, HTTP.UTF_8);
        entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
        request.setEntity(entity);
    }
}
