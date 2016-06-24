package org.esbtools.message.admin.common.utility;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.esbtools.message.admin.common.config.EMAConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class RestRequestUtility {
    private static final Logger LOG = LoggerFactory.getLogger(RestRequestUtility.class);

    private static final String TLSV1 = "TLSv1";

    private static final String[] SUPPORTED_PROTOCOLS = new String[]{TLSV1};
    private static final String[] SUPPORTED_CIPHER_SUITES = null;
    private static final String FILE_PROTOCOL = "file://";

    public static Boolean sendMessageToRestEndPoint( String message, List<String> endpoints ) {
        CloseableHttpClient httpClient;
        try {
            InputStream certAuthorityFile = loadFile(EMAConfiguration.getCaCertificate());

            SSLConnectionSocketFactory sslsf = defaultCertAuthSocketFactory(certAuthorityFile, "fakepwd".toCharArray(), "certAlias");
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            for(String restEndPoint: endpoints ) {
                try {
                    HttpPost httpPost = new HttpPost(restEndPoint);
                    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    httpPost.setEntity(new StringEntity(message.toString()));

                    LOG.debug(httpPost.toString());

                    CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

                    if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                        // status is Success by default
                        return true;
                    } else {
                        // try another host
                        LOG.warn("Message failed to transmit, received HTTP response code:" +
                                httpResponse.getStatusLine().getStatusCode() + " with message:" + httpResponse.getEntity().toString() + " from:" + restEndPoint);
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            httpClient.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    public static SSLConnectionSocketFactory defaultCertAuthSocketFactory(
            InputStream certAuthorityFile, char[] authCertPassword,
            String authCertAlias)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException,
            UnrecoverableKeyException, KeyManagementException {
        X509Certificate cert = getCertificate(certAuthorityFile);
        KeyStore pkcs12KeyStore = getPkcs12KeyStore(authCertPassword);
        KeyStore sunKeyStore = getJksKeyStore(cert, authCertAlias, authCertPassword);
        SSLContext sslContext = getDefaultSSLContext(sunKeyStore, pkcs12KeyStore, authCertPassword);

        return new SSLConnectionSocketFactory(sslContext, SUPPORTED_PROTOCOLS, SUPPORTED_CIPHER_SUITES,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    private static InputStream loadFile(String filePath) throws FileNotFoundException {
        return loadFile(RestRequestUtility.class.getClassLoader(), filePath);
    }

    private static InputStream loadFile(ClassLoader classLoader, String filePath) throws FileNotFoundException {
        if (filePath.startsWith(FILE_PROTOCOL)) {
            return new FileInputStream(filePath.substring(FILE_PROTOCOL.length()));
        }
        return classLoader.getResourceAsStream(filePath);
    }

    private static X509Certificate getCertificate(InputStream certificate)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(certificate);
    }

    private static KeyStore getPkcs12KeyStore(char[] certPassword)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        //empty keystore for now until we enable support for mutual SSL auth
        ks.load(null, certPassword);
        return ks;
    }

    private static KeyStore getJksKeyStore(Certificate certAuthorityFile, String certAlias, char[] certPassword)
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException,
            UnrecoverableKeyException {
        KeyStore jks = KeyStore.getInstance("jks");

        jks.load(null, certPassword);
        jks.setCertificateEntry(certAlias, certAuthorityFile);

        return jks;
    }

    private static SSLContext getDefaultSSLContext(KeyStore trustKeyStore, KeyStore authKeyStore,
                                                   char[] authCertPassword)
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException,
            KeyManagementException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustKeyStore);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(authKeyStore, authCertPassword);

        SSLContext ctx = SSLContext.getInstance(TLSV1);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ctx;
    }

}
