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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.util.Timeout;
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
  void connectAndReadTimeoutsAreApplied() {
    var config = new MarketplaceConfig("http://registry.internal",
        Duration.ofSeconds(3), Duration.ofSeconds(15), Duration.ofSeconds(30));

    var connectionConfig = config.connectionConfig();

    assertEquals(Timeout.ofSeconds(3), connectionConfig.getConnectTimeout());
    assertEquals(Timeout.ofSeconds(15), connectionConfig.getSocketTimeout());
  }

  @Test
  void readTimeoutStopsAStalledRegistryFromHangingTheCaller() throws IOException {
    // Accepts the connection, then never answers.
    try (var blackHole = new ServerSocket(0)) {
      var config = new MarketplaceConfig("http://127.0.0.1:" + blackHole.getLocalPort(),
          Duration.ofSeconds(3), Duration.ofMillis(300), Duration.ofSeconds(30));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
        var ex = assertThrows(RegistryUnreachableException.class,
            () -> client.getCatalogue(null, null));
        assertEquals("127.0.0.1", ex.getHost());
      });
    }
  }

  @Test
  void bodyStallTimesOutInsteadOfHangingTheCaller() throws IOException {
    // 200 + headers, a first chunk, then silence: the response never completes.
    var release = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/api/v1/plugins", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, 4096);
      exchange.getResponseBody().write("{\"plugins\":[".getBytes(StandardCharsets.UTF_8));
      exchange.getResponseBody().flush();
      try {
        release.await(30, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      exchange.close();
    });
    server.start();
    try {
      var config = new MarketplaceConfig("http://127.0.0.1:" + server.getAddress().getPort(),
          Duration.ofSeconds(3), Duration.ofMillis(300), Duration.ofSeconds(30));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
        var ex = assertThrows(RegistryUnreachableException.class,
            () -> client.getCatalogue(null, null));
        assertEquals("127.0.0.1", ex.getHost());
      });
    } finally {
      release.countDown();
      server.stop(0);
    }
  }

  @Test
  void bodyDripIsCutOffByTheWholeRequestDeadline() throws IOException {
    // One byte every 50ms, forever. Every single read lands far inside the 2s socket timeout, so
    // the per-read timeout can never fire and only a whole-exchange deadline ends this.
    var release = new CountDownLatch(1);
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/api/v1/plugins", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, 0);
      try (var body = exchange.getResponseBody()) {
        while (!release.await(50, TimeUnit.MILLISECONDS)) {
          // Whitespace keeps the JSON parser reading and never completes a value.
          body.write(' ');
          body.flush();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (IOException ignored) {
        // The client hung up, which is the point of the test.
      }
      exchange.close();
    });
    server.start();
    try {
      var config = new MarketplaceConfig("http://127.0.0.1:" + server.getAddress().getPort(),
          Duration.ofSeconds(3), Duration.ofSeconds(2), Duration.ofMillis(500));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
        var ex = assertThrows(RegistryUnreachableException.class,
            () -> client.getCatalogue(null, null));
        assertEquals("127.0.0.1", ex.getHost());
      });
    } finally {
      release.countDown();
      server.stop(0);
    }
  }

  @Test
  void headerDripIsCutOffByTheWholeRequestDeadline() throws IOException, InterruptedException {
    // Status line and headers dripped a byte at a time: the response never even starts, so a
    // deadline that only guards the body would never see this exchange.
    try (var server = new ServerSocket(0)) {
      var stop = new CountDownLatch(1);
      var dripper = new Thread(() -> {
        try (var socket = server.accept();
            var out = socket.getOutputStream();
            var in = socket.getInputStream()) {
          while (in.read() != '\n') {
            // Drain the request line; the client sends more, we never need it.
          }
          out.write("HTTP/1.1 200 OK\r\nX-Pad: ".getBytes(StandardCharsets.US_ASCII));
          out.flush();
          // A header line that is never terminated: the response headers never complete, so the
          // client stays blocked in the header-parsing read however long it waits.
          while (!stop.await(50, TimeUnit.MILLISECONDS)) {
            out.write('a');
            out.flush();
          }
        } catch (IOException | InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
      });
      dripper.setDaemon(true);
      dripper.start();
      try {
        var config = new MarketplaceConfig("http://127.0.0.1:" + server.getLocalPort(),
            Duration.ofSeconds(3), Duration.ofSeconds(2), Duration.ofMillis(500));
        var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
            assertThrows(RegistryUnreachableException.class,
                () -> client.getCatalogue(null, null)));
      } finally {
        stop.countDown();
        dripper.join(TimeUnit.SECONDS.toMillis(2));
      }
    }
  }

  @Test
  void slowButCompletingResponseIsNotCutOff() throws IOException, InterruptedException {
    // The budget bounds stalls, not healthy latency. The second call, made after the first call's
    // deadline has passed, also proves a late abort cannot poison the pooled connection.
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/api/v1/plugins", exchange -> {
      try {
        TimeUnit.MILLISECONDS.sleep(300);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      var body = "{\"plugins\":[{\"id\":\"jira\",\"name\":\"Jira\"}]}"
          .getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    server.start();
    try {
      var config = new MarketplaceConfig("http://127.0.0.1:" + server.getAddress().getPort(),
          Duration.ofSeconds(3), Duration.ofSeconds(2), Duration.ofMillis(700));
      var client = new MarketplaceClient(config.marketplaceRestTemplate(), config.registryUrl());

      assertEquals(1, client.getCatalogue(null, null).size());
      TimeUnit.MILLISECONDS.sleep(900);
      assertEquals(1, client.getCatalogue(null, null).size());
    } finally {
      server.stop(0);
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
          Duration.ofSeconds(3), Duration.ofSeconds(5), Duration.ofSeconds(30));
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
