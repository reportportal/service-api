package com.epam.reportportal.base.core.tms.sync.connector.jira;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the default Apache retry strategy (transport-level retries of idempotent requests, {@code Retry-After}
 * support) with a configurable set of retryable status codes and capped exponential backoff.
 * <p>
 * The Jira search endpoint is a read-only {@code POST}, so it is treated as idempotent.
 */
public class JiraHttpRetryStrategy extends DefaultHttpRequestRetryStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraHttpRetryStrategy.class);
  private static final String SEARCH_PATH_SUFFIX = "/rest/api/2/search";

  private final int maxRetries;
  private final long initialIntervalMs;
  private final long maxIntervalMs;
  private final Set<Integer> retryableStatusCodes;

  public JiraHttpRetryStrategy(int maxRetries, long initialIntervalMs, long maxIntervalMs,
      String retryableStatusCodes) {
    this(maxRetries, initialIntervalMs, maxIntervalMs, parseStatusCodes(retryableStatusCodes));
  }

  private JiraHttpRetryStrategy(int maxRetries, long initialIntervalMs, long maxIntervalMs,
      Set<Integer> statusCodes) {
    super(validateMaxRetries(maxRetries), TimeValue.ofMilliseconds(validateInitialInterval(initialIntervalMs)),
        Arrays.asList(InterruptedIOException.class, UnknownHostException.class, ConnectException.class,
            ConnectionClosedException.class, NoRouteToHostException.class, SSLException.class),
        statusCodes);
    if (maxIntervalMs < initialIntervalMs) {
      throw new IllegalArgumentException("Jira retry maxIntervalMs must be greater than or equal to initialIntervalMs");
    }
    this.maxRetries = maxRetries;
    this.initialIntervalMs = initialIntervalMs;
    this.maxIntervalMs = maxIntervalMs;
    this.retryableStatusCodes = statusCodes;
  }

  @Override
  public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
    var retry = super.retryRequest(response, execCount, context);
    if (retry) {
      LOGGER.warn("Jira responded with HTTP {}, retry {}/{} in {} ms", response.getCode(), execCount, maxRetries,
          getRetryInterval(response, execCount, context).toMilliseconds());
    } else if (retryableStatusCodes.contains(response.getCode())) {
      LOGGER.warn("Jira responded with HTTP {}, retries exhausted after {} attempts", response.getCode(), execCount);
    }
    return retry;
  }

  /**
   * Honors {@code Retry-After} (capped by the max interval), otherwise uses capped exponential backoff.
   */
  @Override
  public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
    if (response.getFirstHeader(HttpHeaders.RETRY_AFTER) != null) {
      var retryAfterMs = super.getRetryInterval(response, execCount, context).toMilliseconds();
      return TimeValue.ofMilliseconds(Math.min(retryAfterMs, maxIntervalMs));
    }
    var interval = initialIntervalMs;
    for (var retry = 1; retry < execCount && interval < maxIntervalMs; retry++) {
      interval = Math.min(maxIntervalMs, interval * 2);
    }
    return TimeValue.ofMilliseconds(interval);
  }

  @Override
  protected boolean handleAsIdempotent(HttpRequest request) {
    return super.handleAsIdempotent(request) || isSearchRequest(request);
  }

  private static boolean isSearchRequest(HttpRequest request) {
    var path = request.getPath();
    if (!Method.POST.isSame(request.getMethod()) || path == null) {
      return false;
    }
    var queryStart = path.indexOf('?');
    return (queryStart < 0 ? path : path.substring(0, queryStart)).endsWith(SEARCH_PATH_SUFFIX);
  }

  private static int validateMaxRetries(int maxRetries) {
    if (maxRetries < 0) {
      throw new IllegalArgumentException("Jira retry maxRetries must not be negative");
    }
    return maxRetries;
  }

  private static long validateInitialInterval(long initialIntervalMs) {
    if (initialIntervalMs <= 0) {
      throw new IllegalArgumentException("Jira retry initialIntervalMs must be positive");
    }
    return initialIntervalMs;
  }

  private static Set<Integer> parseStatusCodes(String statusCodes) {
    if (statusCodes == null || statusCodes.isBlank()) {
      throw new IllegalArgumentException("Jira retry statusCodes must not be blank");
    }

    try {
      var parsedCodes = Arrays.stream(statusCodes.split(","))
          .map(String::trim)
          .filter(code -> !code.isEmpty())
          .map(Integer::parseInt)
          .collect(Collectors.toUnmodifiableSet());
      if (parsedCodes.isEmpty() || parsedCodes.stream().anyMatch(code -> code < 100 || code > 599)) {
        throw new IllegalArgumentException("Jira retry statusCodes must contain valid HTTP status codes");
      }
      return parsedCodes;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Jira retry statusCodes must contain valid HTTP status codes", exception);
    }
  }
}
