// TODO: add header comments

package com.adeven.adjustio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class TrackingPackage implements Serializable {
    private static final long serialVersionUID = -35935556512024097L;

    // data
    public String path;
    public String userAgent;
    Map<String, String> parameters;

    // for logging
    public String kind;
    public String successMessage;
    public String failureMessage;

    public String toString() {
        return "k:" + kind +
                " pt:" + path +
                " pr:" + parameters.toString() +    // TODO: format?
                " ua:" + userAgent +
                " sm:" + successMessage +
                " fm:" + failureMessage;
    }

    public void injectEntity(HttpPost request) throws UnsupportedEncodingException {
        if (parameters == null) {
            return;
        }

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entity : parameters.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entity.getKey(), entity.getValue());
            pairs.add(pair);
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs);
        entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
        request.setEntity(entity);
    }

}
