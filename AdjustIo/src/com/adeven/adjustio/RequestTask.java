//
//  RequestTask.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//

package com.adeven.adjustio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.AsyncTask;
import android.util.Log;

public class RequestTask extends AsyncTask<String, String, HttpResponse> {

    private static final String LOGTAG = "AdjustIo";

    private String path;
    private String successMessage;
    private String failureMessage;
    private String userAgent;

    public RequestTask(String path) {
        this.path = path;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    protected HttpResponse doInBackground(String... parameters) {
        HttpClient httpClient = Util.getHttpClient(userAgent);
        HttpPost request = Util.getPostRequest(this.path);

        try {
            request.setEntity(Util.getEntityEncodedParameters(parameters));
            HttpResponse response = httpClient.execute(request);
            return response;
        } catch (SocketException e) {
            Log.d(LOGTAG, "This SDK requires the INTERNET permission. You might need to adjust your manifest. See the README for details.");
        } catch (UnsupportedEncodingException e) {
            Log.d(LOGTAG, "Failed to encode parameters.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(HttpResponse response) {
        if (response == null) {
            Log.d(LOGTAG, failureMessage + " (Request failed. Are you missing the INTERNET permission? See README)");
        } else {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseString = parseResponse(response);

            if (statusCode == HttpStatus.SC_OK) {
                Log.d(LOGTAG, successMessage);
            } else {
                Log.d(LOGTAG, failureMessage + " (" + responseString + ")");
            }
        }
    }

    private String parseResponse(HttpResponse response) {
        String responseString = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            responseString = out.toString().trim();
        } catch (Exception e) {
            return "Failed parsing response";
        }

        return responseString;
    }
}