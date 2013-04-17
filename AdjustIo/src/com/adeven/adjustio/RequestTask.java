//
//  RequestTask.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
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

public class RequestTask extends AsyncTask<String, String, String> {

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

    protected String doInBackground(String... parameters) {
        HttpClient httpClient = Util.getHttpClient(userAgent);
        HttpPost request = Util.getPostRequest(this.path);

        try {
            request.setEntity(Util.getEntityEncodedParameters(parameters));
            HttpResponse response = httpClient.execute(request);
            return getLogString(response);
        } catch (SocketException e) {
            Log.d(Util.LOGTAG, "This SDK requires the INTERNET permission. You might need to adjust your manifest. See the README for details.");
        } catch (UnsupportedEncodingException e) {
            Log.d(Util.LOGTAG, "Failed to encode parameters.");
        } catch (IOException e) {
          Log.d(Util.LOGTAG, "Unexpected IOException", e);
        }

        return null;
    }

    private String getLogString(HttpResponse response) {
        if (response == null) {
            return failureMessage + " (Request failed)";
        } else {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseString = parseResponse(response);

            if (statusCode == HttpStatus.SC_OK) {
                return successMessage;
            } else {
                return failureMessage + " (" + responseString + ")";
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
            e.printStackTrace();
            return "Failed parsing response";
        }

        return responseString;
    }

    protected void onPostExecute(String responseString) {
        Log.d(Util.LOGTAG, responseString);
    }
}
