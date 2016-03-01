package org.esbtools.message.admin.common.utility;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequestUtility {
    private static final Logger LOG = LoggerFactory.getLogger(RestRequestUtility.class);
    
    public static Boolean sendMessageToRestEndPoint( String message, List<String> endpoints ) {
        CloseableHttpClient httpClient;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
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
            LOG.error(e.getMessage());
        }
        return false;
    }
}
