package com.adjust.test;

import static com.adjust.test.Constants.ONE_MINUTE;
import static com.adjust.test.Utils.debug;
import static com.adjust.test.Utils.error;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Networking {
    public static class Response {
        public String response = null;
        public Integer responseCode = null;
        public Map<String, List<String>> headerFields = null;
    }

    // region Injected dependencies
    private final String baseUrl;
    private final Context context;
    // endregion

    // region Internal variables
    private TrustManager[] trustAllCerts;
    private HostnameVerifier hostnameVerifier;;
    private String fingerprint;
    // endregion

    public String clientSdk;
    public String testNames;

    // region Instantiation and Dependencies
    public Networking(final String baseUrl, final Context context) {
        this.baseUrl = baseUrl;
        this.context = context;
        this.fingerprint = fingerprint(context);

        trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  debug("getAcceptedIssuers");

                  return null;
              }

              public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
                  debug("checkClientTrusted");
              }

              public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
                  debug("checkServerTrusted");
              }
          }
        };
        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }
    // endregion

    // region Public API
    public Response sendPost(String path) {
        return sendPost(path, null, null);
    }
    public Response sendPost(String path, String base) {
        return sendPost(path, base, null);
    }
    public Response sendPost(String path, Map<String, String> postBody) {
        return sendPost(path, null, postBody);
    }
    public Response sendPost(String path, String base, Map<String, String> postBody) {
        String merged = base == null ? path : base + path;
        String targetURL = baseUrl + merged;

        try {
            HttpsURLConnection connection = createPOSTHttpsURLConnection(
              targetURL, postBody);
            Response response = readHttpResponse(connection);
            debug("Response: %s", response.response);

            response.headerFields= connection.getHeaderFields();
            debug("Headers: %s", response.headerFields);

            return response;
        } catch (IOException e) {
            error(e.getMessage());
        } catch (Exception e) {
            error(e.getMessage());
        }
        return null;
    }
    // endregion

    // region Internal Methods
    private HttpsURLConnection createPOSTHttpsURLConnection(
      String urlString,
      Map<String, String> postBody)
      throws IOException
    {
        DataOutputStream wr = null;
        HttpsURLConnection connection = null;

        try {
            debug("POST request: %s", urlString);
            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();

            applyConnectionOptions(connection);

            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (postBody != null && postBody.size() > 0) {
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(getPostDataString(postBody));
            }

            return connection;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private void applyConnectionOptions(HttpsURLConnection connection) {
        if (clientSdk != null) {
            connection.setRequestProperty("Client-SDK", clientSdk);
        }
        if (testNames != null) {
            connection.setRequestProperty("Test-Names", testNames);
        }
        if (fingerprint != null) {
            connection.setRequestProperty("Fingerprint", fingerprint);
        }

        //Inject local ip address for Jenkins script
        connection.setRequestProperty("Local-Ip", getIPAddress(true));

        connection.setConnectTimeout(ONE_MINUTE);
        connection.setReadTimeout(ONE_MINUTE);
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());

            connection.setHostnameVerifier(hostnameVerifier);
            debug("applyConnectionOptions");
        } catch (Exception e) {
            debug("applyConnectionOptions %s", e.getMessage());
        }
    }

    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            error("Failed to read ip address (%s)", ex.getMessage());
        }

        return "";
    }

    private static String getPostDataString(Map<String, String> body) throws
      UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : body.entrySet()) {
            String encodedName = URLEncoder.encode(entry.getKey(), Constants.ENCODING);
            String value = entry.getValue();
            String encodedValue = value != null ? URLEncoder.encode(value, Constants.ENCODING) : "";

            if (result.length() > 0) {
                result.append("&");
            }

            result.append(encodedName);
            result.append("=");
            result.append(encodedValue);
        }

        return result.toString();
    }

    static Response readHttpResponse(HttpsURLConnection connection) throws Exception {
        StringBuffer sb = new StringBuffer();
        Response response = new Response();

        try {
            connection.connect();

            response.responseCode = connection.getResponseCode();
            InputStream inputStream;

            if (response.responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            error("Failed to read response. (%s)", e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        response.response = sb.toString();
        return response;
    }

    private static String fingerprint(final Context context) {
        if (context == null) {
            return null;
        }

        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                final PackageInfo packageInfo = packageManager.getPackageInfo(
                  packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (packageInfo == null || packageInfo.signingInfo == null) {
                    return null;
                }
                if (packageInfo.signingInfo.hasMultipleSigners()){
                    return signatureDigestList(packageInfo.signingInfo.getApkContentsSigners());
                }
                else{
                    return signatureDigestList(packageInfo.signingInfo.getSigningCertificateHistory());
                }
            }
            else {
                @SuppressLint("PackageManagerGetSignatures")
                final PackageInfo packageInfo = packageManager.getPackageInfo(
                  packageName, PackageManager.GET_SIGNATURES);
                return signatureDigestList(packageInfo.signatures);
            }

        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String signatureDigest(final Signature signature) {
        if (signature == null) {
            return null;
        }

        try {
            //return toHex(
            //MessageDigest.getInstance("SHA1").digest(signature.toByteArray()));
            final byte[] signatureByteArray = signature.toByteArray();
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            final byte[] digest = md.digest(signatureByteArray);
            final String hex = toHex(digest);
            return hex.toLowerCase();
            //return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
    }
    private static String signatureDigestList(final Signature[] sigList) {
        for (final Signature signature: sigList) {
            return signatureDigest(signature);
        }
        return null;
    }

    public static String toHex(final byte[] bytes) {
        return String.format("%0" + (bytes.length << 1) + "X",
          new BigInteger(1, bytes));
    }

    // endregion
}
