package org.apache.cordova.mqttclient;

import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cordova.CordovaWebView;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.*;
/**
 * Created by shenlele on 2017/4/20.
 */

public class SslUtil {
    /**
     * 用来获取SSLSocketFactory
     * @param webView
     * @param caCrtFile ca证书路径
     * @param crtFile   cert证书路径
     * @param keyFile   key证书路径
     * @param password  生成证书时的密码
     * @return          SSLSocketFactory
     * @throws Exception
     */
    static SSLSocketFactory getSocketFactory(CordovaWebView webView, final String caCrtFile, final String crtFile, final String keyFile,
                                             final String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMReader reader = new PEMReader(
                new InputStreamReader(webView.getContext().getAssets().open(caCrtFile)));
        X509Certificate caCert = (X509Certificate) reader.readObject();
        reader.close();

        // load client certificate
        reader = new PEMReader(new InputStreamReader(webView.getContext().getAssets().open(crtFile)));
        X509Certificate cert = (X509Certificate) reader.readObject();
        reader.close();

        // load client private key
        reader = new PEMReader(new InputStreamReader(webView.getContext().getAssets().open(keyFile)),
                new PasswordFinder() {
                    @Override
                    public char[] getPassword() {
                        return password.toCharArray();
                    }
                });
        KeyPair key = (KeyPair) reader.readObject();
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[] { cert });
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }
}
