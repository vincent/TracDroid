package com.byarger.exchangeit;

/*
 * From http://code.google.com/p/android/issues/detail?id=1946#c10 
 */


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class TrivialTrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
