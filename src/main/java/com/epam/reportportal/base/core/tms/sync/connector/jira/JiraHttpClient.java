package com.epam.reportportal.base.core.tms.sync.connector.jira;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class JiraHttpClient implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraHttpClient.class);

    private final CloseableHttpClient httpClient;

    public JiraHttpClient() {
        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(1L)))
                .build();
    }

    public InputStream downloadAttachmentStream(String bearerToken, String contentUrl) throws IOException {
        LOGGER.info("Downloading attachment from URL: {}", contentUrl);

        var request = new HttpGet(contentUrl);
        request.setHeader("Authorization", "Bearer " + bearerToken);
        request.setHeader("User-Agent", "ReportPortal-TMS-Sync/1.0");

        CloseableHttpResponse response = httpClient.execute(request);

        if (response.getCode() >= 400) {
            EntityUtils.consumeQuietly(response.getEntity());
            response.close();
            throw new IOException("Failed to download attachment. HTTP Code: " + response.getCode());
        }

        if (response.getEntity() == null) {
            response.close();
            throw new IOException("Response body is null for URL: " + contentUrl);
        }

        // Wrap the InputStream to ensure the CloseableHttpResponse is closed when the stream is closed
        InputStream originalStream = response.getEntity().getContent();
        return new FilterInputStream(originalStream) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    response.close();
                }
            }
        };
    }

    public String get(String jiraBaseUrl, String bearerToken, String path) {
        var url = jiraBaseUrl.endsWith("/") ? jiraBaseUrl + path : jiraBaseUrl + "/" + path;
        
        var request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + bearerToken);
        request.setHeader("Accept", "application/json");
        request.setHeader("User-Agent", "ReportPortal-TMS-Sync/1.0");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() >= 400) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, 
                    "Jira API request failed. HTTP Code: " + response.getCode());
            }
            if (response.getEntity() == null) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Jira API response body is null");
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to execute Jira API request: " + e.getMessage());
        }
    }

    public String post(String jiraBaseUrl, String bearerToken, String path, String jsonBody) {
        var url = jiraBaseUrl.endsWith("/") ? jiraBaseUrl + path : jiraBaseUrl + "/" + path;
        
        var request = new HttpPost(url);
        request.setHeader("Authorization", "Bearer " + bearerToken);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("User-Agent", "ReportPortal-TMS-Sync/1.0");
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() >= 400) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, 
                    "Jira API request failed. HTTP Code: " + response.getCode());
            }
            if (response.getEntity() == null) {
                throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Jira API response body is null");
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to execute Jira API request: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close Jira HTTP client", e);
        }
    }
}