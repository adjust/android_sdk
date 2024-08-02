package com.adjust.test_options;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * TODO: Add a class header comment!
 */
public class TLSSocketFactory
        extends SSLSocketFactory
{
    private SSLSocketFactory internalSSLSocketFactory;

    public TLSSocketFactory(final TrustManager[] trustCerts,
                            final SecureRandom secureRandom)
            throws
            KeyManagementException,
            NoSuchAlgorithmException
    {

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustCerts, secureRandom);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(final Socket s,
                               final String host,
                               final int port,
                               final boolean autoClose)
            throws IOException
    {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(final String host,
                               final int port)
            throws IOException
    {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final String host,
                               final int port,
                               final InetAddress localHost,
                               final int localPort)
            throws IOException
    {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(
                host,
                port,
                localHost,
                localPort));
    }

    @Override
    public Socket createSocket(final InetAddress host,
                               final int port)
            throws IOException
    {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final InetAddress address,
                               final int port,
                               final InetAddress localAddress,
                               final int localPort)
            throws IOException
    {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(
                address,
                port,
                localAddress,
                localPort));
    }

    private Socket enableTLSOnSocket(final Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }
}
