/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.core.marketplace.DeadlineHttpRequestFactory;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceJson;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the marketplace registry only. Separate from the shared
 * {@link RestTemplateConfig} template, which has no timeouts — a black-holed registry must not
 * stall request threads.
 */
@Configuration
public class MarketplaceConfig {

  private final String registryUrl;
  private final Duration connectTimeout;
  private final Duration readTimeout;
  private final Duration requestDeadline;
  private PoolingHttpClientConnectionManager connectionManager;
  private CloseableHttpClient httpClient;
  private final ScheduledExecutorService deadlineWatchdog =
      Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "marketplace-deadline");
        thread.setDaemon(true);
        return thread;
      });

  /**
   * Reads the marketplace client settings.
   *
   * @param registryUrl     registry base URL
   * @param connectTimeout  TCP connect timeout
   * @param readTimeout     socket read timeout, bounding a single read
   * @param requestDeadline whole-exchange deadline, bounding all reads together
   */
  public MarketplaceConfig(
      @Value("${marketplace.registry.url:https://marketplace.reportportal.io}") String registryUrl,
      @Value("${marketplace.client.connect-timeout:PT3S}") Duration connectTimeout,
      @Value("${marketplace.client.read-timeout:PT15S}") Duration readTimeout,
      @Value("${marketplace.client.request-deadline:PT30S}") Duration requestDeadline) {
    this.registryUrl = registryUrl;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
    this.requestDeadline = requestDeadline;
  }

  /** The pool holds its connections open until told otherwise, so hand them back on the way out. */
  @PreDestroy
  void shutdown() {
    deadlineWatchdog.shutdownNow();
    if (httpClient != null) {
      httpClient.close(CloseMode.GRACEFUL);
    }
  }

  public String registryUrl() {
    return registryUrl;
  }

  /**
   * The read timeout is a socket timeout, so it bounds every read of the response — a registry
   * that sends headers and then stalls mid-body fails instead of pinning the caller. The JDK
   * client is not used here: its request timeout only closes the body stream, and the stall then
   * surfaces as an unreadable response rather than a timeout.
   */
  ConnectionConfig connectionConfig() {
    return ConnectionConfig.custom()
        .setConnectTimeout(Timeout.of(connectTimeout))
        .setSocketTimeout(Timeout.of(readTimeout))
        .build();
  }

  CloseableHttpClient httpClient() {
    connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
        .setDefaultConnectionConfig(connectionConfig())
        .setMaxConnPerRoute(20)
        .setMaxConnTotal(20)
        .build();
    httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        // Queuing for a pooled connection is another way to stall a request thread; the default
        // wait is three minutes.
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.of(connectTimeout))
            .build())
        // The artifact route's 302 is data: the Location must be read, not chased.
        .disableRedirectHandling()
        .build();
    return httpClient;
  }

  /**
   * Marketplace-only template. Named on purpose so the shared {@code restTemplate} bean keeps
   * winning by-name injection elsewhere.
   */
  @Bean("marketplaceRestTemplate")
  public RestTemplate marketplaceRestTemplate() {
    var requestFactory =
        new DeadlineHttpRequestFactory(httpClient(), requestDeadline, deadlineWatchdog);
    var restTemplate = new RestTemplate(List.of(
        new ByteArrayHttpMessageConverter(),
        new StringHttpMessageConverter(StandardCharsets.UTF_8),
        new MappingJackson2HttpMessageConverter(MarketplaceJson.mapper())));
    restTemplate.setRequestFactory(requestFactory);
    return restTemplate;
  }

  @Bean
  public MarketplaceClient marketplaceClient(
      @Qualifier("marketplaceRestTemplate") RestTemplate marketplaceRestTemplate) {
    return new MarketplaceClient(marketplaceRestTemplate, registryUrl);
  }
}
