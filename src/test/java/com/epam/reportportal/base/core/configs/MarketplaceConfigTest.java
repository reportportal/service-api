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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.health.JobsHealthIndicator;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Timeout, redirect and bean-wiring behaviour of the marketplace HTTP client.
 */
class MarketplaceConfigTest {

  @Test
  void connectTimeoutIsApplied() {
    var config = new MarketplaceConfig("http://registry.internal",
        Duration.ofSeconds(3), Duration.ofSeconds(15));

    HttpClient httpClient = config.httpClient();

    assertEquals(Duration.ofSeconds(3), httpClient.connectTimeout().orElse(null));
  }

  @Test
  void readTimeoutStopsAStalledRegistryFromHangingTheCaller() throws IOException {
    // Accepts the connection, then never answers.
    try (var blackHole = new ServerSocket(0)) {
      var config = new MarketplaceConfig("http://127.0.0.1:" + blackHole.getLocalPort(),
          Duration.ofSeconds(3), Duration.ofMillis(300));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
        var ex = assertThrows(RegistryUnreachableException.class,
            () -> client.getCatalogue(null, null));
        assertEquals("127.0.0.1", ex.getHost());
      });
    }
  }

  @Test
  void artifactRedirectIsNotFollowed() throws IOException {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    var cdnUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/cdn/jira-1.4.2.jar";
    server.createContext("/api/v1/plugins/jira/versions/1.4.2/artifact", exchange -> {
      exchange.getResponseHeaders().add("Location", cdnUrl);
      exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
      var body = ("<a href=\"" + cdnUrl + "\">Found</a>.\n").getBytes();
      exchange.sendResponseHeaders(302, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    // Followed redirects would land here and return a body the client must never see.
    server.createContext("/cdn/jira-1.4.2.jar", exchange -> {
      var body = "{\"downloadUrl\":\"followed-the-redirect\"}".getBytes();
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    server.start();
    try {
      var config = new MarketplaceConfig("http://127.0.0.1:" + server.getAddress().getPort(),
          Duration.ofSeconds(3), Duration.ofSeconds(5));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      var artifact = client.resolveArtifact("jira", "1.4.2", null);

      assertEquals(cdnUrl, artifact.downloadUrl());
      assertNull(artifact.expiresAt());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void marketplaceTemplateDoesNotHijackTheSharedRestTemplate() {
    try (var ctx = new AnnotationConfigApplicationContext()) {
      ctx.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
      ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test",
          Map.of("rp.jobs.baseUrl", "http://jobs")));
      ctx.register(PropertySourcesPlaceholderConfigurer.class, RestTemplateConfig.class,
          MarketplaceConfig.class, JobsHealthIndicator.class);
      ctx.refresh();

      var shared = ctx.getBean("restTemplate", RestTemplate.class);
      var marketplace = ctx.getBean("marketplaceRestTemplate", RestTemplate.class);
      var healthIndicator = ctx.getBean(JobsHealthIndicator.class);

      assertSame(shared, ReflectionTestUtils.getField(healthIndicator, "restTemplate"));
      assertSame(marketplace, ReflectionTestUtils.getField(
          ctx.getBean(MarketplaceClient.class), "restTemplate"));
    }
  }
}
