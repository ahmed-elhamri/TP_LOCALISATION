package com.example.tp_localisation.helpers;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;

public class SSLHelper {

    public static SSLSocketFactory getUnSafeSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustAllCertificates, new SecureRandom());

        // Return the SSLSocketFactory object
        return context.getSocketFactory();
    }
}
