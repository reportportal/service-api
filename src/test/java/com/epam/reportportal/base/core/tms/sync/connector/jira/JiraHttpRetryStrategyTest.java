package com.epam.reportportal.base.core.tms.sync.connector.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.Test;

class JiraHttpRetryStrategyTest {

  private static final HttpCoreContext CONTEXT = HttpCoreContext.create();

  @Test
  void retriesOnlyConfiguredTransientStatusCodesUpToConfiguredLimit() {
    var strategy = new JiraHttpRetryStrategy(3, 100, 1_000, "408,429,504");

    assertTrue(strategy.retryRequest(new BasicHttpResponse(504), 1, CONTEXT));
    assertTrue(strategy.retryRequest(new BasicHttpResponse(429), 3, CONTEXT));
    assertFalse(strategy.retryRequest(new BasicHttpResponse(504), 4, CONTEXT));
    assertFalse(strategy.retryRequest(new BasicHttpResponse(500), 1, CONTEXT));
  }

  @Test
  void calculatesCappedExponentialBackoff() {
    var strategy = new JiraHttpRetryStrategy(3, 100, 350, "504");
    var response = new BasicHttpResponse(504);

    assertEquals(100, strategy.getRetryInterval(response, 1, CONTEXT).toMilliseconds());
    assertEquals(200, strategy.getRetryInterval(response, 2, CONTEXT).toMilliseconds());
    assertEquals(350, strategy.getRetryInterval(response, 3, CONTEXT).toMilliseconds());
    assertEquals(350, strategy.getRetryInterval(response, 50, CONTEXT).toMilliseconds());
  }

  @Test
  void honorsRetryAfterHeaderCappedByMaxInterval() {
    var strategy = new JiraHttpRetryStrategy(3, 100, 5_000, "429,503");
    var response = new BasicHttpResponse(429);

    response.setHeader(HttpHeaders.RETRY_AFTER, "2");
    assertEquals(2_000, strategy.getRetryInterval(response, 1, CONTEXT).toMilliseconds());

    response.setHeader(HttpHeaders.RETRY_AFTER, "120");
    assertEquals(5_000, strategy.getRetryInterval(response, 1, CONTEXT).toMilliseconds());

    response.setHeader(HttpHeaders.RETRY_AFTER, "garbage");
    assertEquals(100, strategy.getRetryInterval(response, 1, CONTEXT).toMilliseconds());
  }

  @Test
  void retriesTransientTransportExceptionsOfIdempotentRequests() {
    var strategy = new JiraHttpRetryStrategy(3, 100, 1_000, "504");
    var get = new BasicHttpRequest("GET", "/rest/api/2/serverInfo");

    assertTrue(strategy.retryRequest(get, new SocketException("connection reset"), 1, CONTEXT));
    assertFalse(strategy.retryRequest(get, new SocketException("connection reset"), 4, CONTEXT));
    assertFalse(strategy.retryRequest(get, new SocketTimeoutException("read timed out"), 1, CONTEXT));
    assertFalse(strategy.retryRequest(get, new IOException("other"), 4, CONTEXT));
  }

  @Test
  void treatsOnlySearchPostAsIdempotent() {
    var strategy = new JiraHttpRetryStrategy(3, 100, 1_000, "504");
    var exception = new SocketException("connection reset");

    assertTrue(strategy.retryRequest(new BasicHttpRequest("POST", "/jira/rest/api/2/search"), exception, 1, CONTEXT));
    assertFalse(strategy.retryRequest(new BasicHttpRequest("POST", "/rest/api/2/issue"), exception, 1, CONTEXT));
  }

  @Test
  void rejectsInvalidConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(-1, 100, 1_000, "504"));
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(1, 0, 1_000, "504"));
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(1, 200, 100, "504"));
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(1, 100, 1_000, "504,invalid"));
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(1, 100, 1_000, " "));
    assertThrows(IllegalArgumentException.class, () -> new JiraHttpRetryStrategy(1, 100, 1_000, "99"));
  }
}
