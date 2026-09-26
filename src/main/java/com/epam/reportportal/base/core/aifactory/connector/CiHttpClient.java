package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
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

/**
 * Minimal HTTPS-only POST client shared by the CI trigger connectors.
 */
@Component
public class CiHttpClient implements DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(CiHttpClient.class);

  private final CloseableHttpClient httpClient;

  public CiHttpClient() {
    var requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofSeconds(10))
        .setResponseTimeout(Timeout.ofSeconds(30))
        .build();

    var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
        .setMaxConnTotal(50)
        .setMaxConnPerRoute(10)
        .build();

    this.httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .setRetryStrategy(new DefaultHttpRequestRetryStrategy(2, TimeValue.ofSeconds(1L)))
        .disableRedirectHandling()
        .build();
  }

  /**
   * Executes an HTTPS POST with a JSON body and returns the response body
   * (empty string if there is none), so callers that need it (e.g. to read a
   * created resource's URL out of the response) don't have to re-request it.
   *
   * @param url absolute HTTPS URL
   * @param headers request headers (e.g. Authorization)
   * @param jsonBody request body, already serialized as JSON
   */
  public String postForStatus(String url, Map<String, String> headers, String jsonBody) {
    validateHttpsUrl(url);
    var request = new HttpPost(url);
    headers.forEach(request::setHeader);
    request.setHeader("Accept", "application/json");
    request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
      if (response.getCode() >= 400) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "CI trigger request failed. HTTP code: " + response.getCode() + ". " + body);
      }
      return body;
    } catch (IOException | ParseException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Failed to execute CI trigger request: " + e.getMessage());
    }
  }

  private void validateHttpsUrl(String url) {
    try {
      URI uri = new URI(url);
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
        throw new URISyntaxException(url, "URL must use HTTPS and include a host");
      }
    } catch (URISyntaxException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "CI trigger URL must be a valid HTTPS URL");
    }
  }

  @Override
  public void destroy() {
    try {
      httpClient.close();
    } catch (IOException e) {
      LOGGER.error("Failed to close CI HTTP client", e);
    }
  }
}
