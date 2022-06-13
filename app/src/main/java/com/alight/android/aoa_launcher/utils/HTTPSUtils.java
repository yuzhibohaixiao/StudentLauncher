package com.alight.android.aoa_launcher.utils;

import android.content.Context;

import com.alight.android.aoa_launcher.net.urls.HeaderInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by wangzhe
 */
public class HTTPSUtils {
    private static volatile OkHttpClient client;

    /**
     * 获取OkHttpClient实例
     *
     * @return
     */
    public static OkHttpClient getInstance(Context context) {
        if (client == null) {
            synchronized (OkHttpClient.class) {
                if (client == null) {
                    new HTTPSUtils(context);
                    return client;
                }
            }
        }
        return client;
    }

    /**
     * 初始化HTTPS,添加信任证书
     *
     * @param context
     */
    private HTTPSUtils(Context context) {
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        final InputStream inputStream;
        try {
            inputStream = context.getAssets().open("alightca.cer"); // 得到证书的输入流
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    /**
                     * Given the partial or complete certificate chain provided by the
                     * peer, build a certificate path to a trusted root and return if
                     * it can be validated and is trusted for client SSL
                     * authentication based on the authentication type.
                     * <p>
                     * The authentication type is determined by the actual certificate
                     * used. For instance, if RSAPublicKey is used, the authType
                     * should be "RSA". Checking is case-sensitive.
                     *
                     * @param chain    the peer certificate chain
                     * @param authType the authentication type based on the client certificate
                     * @throws IllegalArgumentException if null or zero-length chain
                     *                                  is passed in for the chain parameter or if null or zero-length
                     *                                  string is passed in for the  authType parameter
                     * @throws CertificateException     if the certificate chain is not trusted
                     *                                  by this TrustManager.
                     */
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    /**
                     * Given the partial or complete certificate chain provided by the
                     * peer, build a certificate path to a trusted root and return if
                     * it can be validated and is trusted for server SSL
                     * authentication based on the authentication type.
                     * <p>
                     * The authentication type is the key exchange algorithm portion
                     * of the cipher suites represented as a String, such as "RSA",
                     * "DHE_DSS". Note: for some exportable cipher suites, the key
                     * exchange algorithm is determined at run time during the
                     * handshake. For instance, for TLS_RSA_EXPORT_WITH_RC4_40_MD5,
                     * the authType should be RSA_EXPORT when an ephemeral RSA key is
                     * used for the key exchange, and RSA when the key from the server
                     * certificate is used. Checking is case-sensitive.
                     *
                     * @param chain    the peer certificate chain
                     * @param authType the key exchange algorithm used
                     * @throws IllegalArgumentException if null or zero-length chain
                     *                                  is passed in for the chain parameter or if null or zero-length
                     *                                  string is passed in for the  authType parameter
                     * @throws CertificateException     if the certificate chain is not trusted
                     *                                  by this TrustManager.
                     */
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }};
                //以流的方式读入证书
                trustManager = trustManagerForCertificates(inputStream);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
//                sslContext.init(null, new TrustManager[]{trustManager}, null);
                sslSocketFactory = sslContext.getSocketFactory();

            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            client = new OkHttpClient.Builder()
                    .hostnameVerifier((hostname, session) -> true)
                    .addInterceptor(log)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(new HeaderInterceptor())
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 以流的方式添加信任证书
     */
    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     * <p>
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     * <p>
     * <p>
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     * <p>
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     */
    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }


    /**
     * 添加password
     *
     * @param password
     * @return
     * @throws GeneralSecurityException
     */
    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // 这里添加自定义的密码，默认
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }


}