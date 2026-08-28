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

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceJson;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
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

  /**
   * Reads the marketplace client settings.
   *
   * @param registryUrl    registry base URL
   * @param connectTimeout TCP connect timeout
   * @param readTimeout    response timeout
   */
  public MarketplaceConfig(
      @Value("${marketplace.registry.url:https://marketplace.reportportal.io}") String registryUrl,
      @Value("${marketplace.client.connect-timeout:PT3S}") Duration connectTimeout,
      @Value("${marketplace.client.read-timeout:PT15S}") Duration readTimeout) {
    this.registryUrl = registryUrl;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  public String registryUrl() {
    return registryUrl;
  }

  HttpClient httpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(connectTimeout)
        // The artifact route's 302 is data: the Location must be read, not chased.
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }

  /**
   * Marketplace-only template. Named on purpose so the shared {@code restTemplate} bean keeps
   * winning by-name injection elsewhere.
   */
  @Bean("marketplaceRestTemplate")
  public RestTemplate marketplaceRestTemplate() {
    var requestFactory = new JdkClientHttpRequestFactory(httpClient());
    requestFactory.setReadTimeout(readTimeout);
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
