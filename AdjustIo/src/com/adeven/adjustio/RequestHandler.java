package com.adeven.adjustio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class RequestHandler extends HandlerThread {
    private static final int CONNECTION_TIMEOUT = 1000 * 5; // 5 seconds TODO: time 1 minute
    private static final int SOCKET_TIMEOUT = 1000 * 5; // 5 seconds TODO: time 1 minute

    private static final int MESSAGE_ARG_INIT = 72401;
    private static final int MESSAGE_ARG_SEND = 72400;

    private InternalHandler internalHandler;
    private PackageHandler packageHandler;
    private HttpClient httpClient;

    protected RequestHandler(PackageHandler packageHandler) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.internalHandler = new InternalHandler(getLooper(), this);
        this.packageHandler = packageHandler;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        internalHandler.sendMessage(message);
    }

    protected void sendPackage(ActivityPackage pack) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_SEND;
        message.obj = pack;
        internalHandler.sendMessage(message);
    }

    private static final class InternalHandler extends Handler {
        private final WeakReference<RequestHandler> requestHandlerReference;

        protected InternalHandler(Looper looper, RequestHandler requestHandler) {
            super(looper);
            this.requestHandlerReference = new WeakReference<RequestHandler>(requestHandler);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            RequestHandler requestHandler = requestHandlerReference.get();
            if (requestHandler == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_INIT:
                requestHandler.initInternal();
                break;
            case MESSAGE_ARG_SEND:
                ActivityPackage activityPackage = (ActivityPackage) message.obj;
                requestHandler.sendInternal(activityPackage);
                break;
            }
        }
    }

    // TODO: use SSLSessionCache?
    // http://candrews.integralblue.com/2011/09/best-way-to-use-httpclient-in-android/
    private void initInternal() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);
        httpClient = new DefaultHttpClient(httpParams);
    }

    private void sendInternal(ActivityPackage activityPackage) {
        try {
            HttpUriRequest request = getRequest(activityPackage);
            HttpResponse response = httpClient.execute(request);
            requestFinished(response, activityPackage);
        }
        catch (UnsupportedEncodingException e) {
            sendNextPackage(activityPackage, "Failed to encode parameters", e);
        }
        catch (ClientProtocolException e) {
            closePackage(activityPackage, "Client protocol error", e);
        }
        catch (SocketTimeoutException e) {
            closePackage(activityPackage, "Request timed out", e);
        }
        catch (IOException e) {
            closePackage(activityPackage, "Request failed", e);
        }
        catch (Exception e) {
            sendNextPackage(activityPackage, "Runtime exeption", e);
        }
    }

    private void requestFinished(HttpResponse response, ActivityPackage activityPackage) {
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = parseResponse(response);

        if (statusCode == HttpStatus.SC_OK) {
            Logger.info(activityPackage.getSuccessMessage());
        } else {
            Logger.error(String.format("%s. (%s)", activityPackage.getFailureMessage(), responseString));
        }

        packageHandler.sendNextPackage();
    }

    private String parseResponse(HttpResponse response) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            String responseString = out.toString().trim();
            return responseString;
        }
        catch (Exception e) {
            Logger.error(String.format("Failed to parse response (%s)", e));
            return "Failed to parse response";
        }
    }

    private void closePackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        String failureMessage = activityPackage.getFailureMessage();
        if (throwable != null) {
            Logger.error(String.format("%s. (%s: %s) Will retry later.", failureMessage, message, throwable));
        } else {
            Logger.error(String.format("%s. (%s) Will retry later.", failureMessage, message));
        }
        packageHandler.closeFirstPackage();
    }

    private void sendNextPackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        String failureMessage = activityPackage.getFailureMessage();
        if (throwable != null) {
            Logger.error(String.format("%s (%s: %s)", failureMessage, message, throwable));
        } else {
            Logger.error(String.format("%s (%s)", failureMessage, message));
        }

        packageHandler.sendNextPackage();
    }


    private HttpUriRequest getRequest(ActivityPackage activityPackage) throws UnsupportedEncodingException {
        String url = Util.BASE_URL + activityPackage.path;
        HttpPost request = new HttpPost(url);

        String language = Locale.getDefault().getLanguage();
        request.addHeader("User-Agent", activityPackage.userAgent);
        request.addHeader("Client-SDK", activityPackage.clientSdk);
        request.addHeader("Accept-Language", language);

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entity : activityPackage.parameters.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entity.getKey(), entity.getValue());
            pairs.add(pair);
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs);
        entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
        request.setEntity(entity);

        return request;
    }
}
