package com.oracle.determinations.mobile.application;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustAllCerts {

    /**
     * Trust manager that does not perform any checks.
     */
    private static class NullX509TrustManager implements X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType)
                throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType)
                throws java.security.cert.CertificateException {
        }
    }

    /**
     * Host name verifier that does not perform any checks.
     */
    private static class NullHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return true;
        }
    }

    /**
     * Disable trust checks for SSL connections.
     */
    public void init() {
        try {
            new URL("https://0.0.0.0/").getContent();
        } catch (IOException e) {
            System.out.println("-------------------------------enable https trust--------------------------------");
            // This invocation will always fail, but it will register the
            // default SSL provider to the URL class.
        }

        try {
            SSLContext sslc;

            sslc = SSLContext.getInstance("TLS");

            final TrustManager[] trustManagerArray = { new NullX509TrustManager() };
            sslc.init(null, trustManagerArray, null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
