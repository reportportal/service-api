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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.configs.MarketplaceConfig;
import com.epam.reportportal.base.core.marketplace.exception.RegistryProtocolException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * The artifact download: bytes to disk, redirects followed, a dead host still named.
 */
class HttpMarketplaceArtifactFetcherTest {

  private static final byte[] JAR = "PK pretend jar".getBytes(StandardCharsets.UTF_8);

  private static MarketplaceConfig config(String url) {
    return new MarketplaceConfig(url, Duration.ofSeconds(3), Duration.ofSeconds(5),
        Duration.ofSeconds(30), false);
  }

  @Test
  void streamsTheArtifactToDiskAndFollowsTheCdnRedirect() throws IOException {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    // The registry already resolved its own 302; this is the CDN redirecting again, which is not
    // data to be read but a hop to be taken.
    server.createContext("/artifact", exchange -> {
      exchange.getResponseHeaders().add("Location", "/cdn/jira-1.4.2.jar");
      exchange.sendResponseHeaders(302, -1);
      exchange.close();
    });
    server.createContext("/cdn/jira-1.4.2.jar", exchange -> {
      exchange.sendResponseHeaders(200, JAR.length);
      exchange.getResponseBody().write(JAR);
      exchange.close();
    });
    server.start();
    var target = Files.createTempFile("fetcher-test-", ".jar");
    try {
      var fetcher = config("http://127.0.0.1:" + server.getAddress().getPort())
          .marketplaceArtifactFetcher(Duration.ofSeconds(30), null);

      fetcher.fetch("http://127.0.0.1:" + server.getAddress().getPort() + "/artifact", target);

      assertArrayEquals(JAR, Files.readAllBytes(target));
    } finally {
      Files.deleteIfExists(target);
      server.stop(0);
    }
  }

  /**
   * The checksum that would catch a substituted artifact runs only once every byte is on disk, and
   * the download follows redirects, so the host answering last is not necessarily the registry. An
   * unbounded body could fill the filesystem before anything rejected it — and a full filesystem
   * is not one failed install, it is every request the service is serving.
   */
  @Test
  void bodyLargerThanTheBoundIsRefusedInsteadOfWritten() throws IOException {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/artifact", exchange -> {
      // chunked, with no Content-Length to reject it by: the size is only knowable while reading
      exchange.sendResponseHeaders(200, 0);
      try (var body = exchange.getResponseBody()) {
        for (var i = 0; i < 64; i++) {
          body.write(new byte[1024]);
        }
      }
      exchange.close();
    });
    server.start();
    var target = Files.createTempFile("fetcher-test-", ".jar");
    try {
      var url = "http://127.0.0.1:" + server.getAddress().getPort() + "/artifact";
      var fetcher = config(url).marketplaceArtifactFetcher(Duration.ofSeconds(30), 4096L);

      var failure = assertThrows(RegistryProtocolException.class, () -> fetcher.fetch(url, target));

      assertTrue(failure.getMessage().contains("4096"), failure.getMessage());
      // nothing partial is left behind: it would hand the install path a truncated jar, and keep
      // the disk space that was the problem to begin with
      assertFalse(Files.exists(target));
    } finally {
      Files.deleteIfExists(target);
      server.stop(0);
    }
  }

  @Test
  void bodyInsideTheBoundIsWrittenWhole() throws IOException {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/artifact", exchange -> {
      exchange.sendResponseHeaders(200, JAR.length);
      exchange.getResponseBody().write(JAR);
      exchange.close();
    });
    server.start();
    var target = Files.createTempFile("fetcher-test-", ".jar");
    try {
      var url = "http://127.0.0.1:" + server.getAddress().getPort() + "/artifact";
      var fetcher = config(url).marketplaceArtifactFetcher(Duration.ofSeconds(30), 4096L);

      fetcher.fetch(url, target);

      assertArrayEquals(JAR, Files.readAllBytes(target));
    } finally {
      Files.deleteIfExists(target);
      server.stop(0);
    }
  }

  @Test
  void anEndlessRedirectChainIsCutOffRatherThanFollowed() throws IOException {
    // Every hop is a request this instance makes to a host the previous answer chose, so the chain
    // is bounded. A fresh path each time keeps the circular-redirect check out of the way.
    var hops = new AtomicInteger();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/hop", exchange -> {
      exchange.getResponseHeaders().add("Location", "/hop/" + hops.incrementAndGet());
      exchange.sendResponseHeaders(302, -1);
      exchange.close();
    });
    server.start();
    var target = Files.createTempFile("fetcher-test-", ".jar");
    try {
      var fetcher = config("http://127.0.0.1:" + server.getAddress().getPort())
          .marketplaceArtifactFetcher(Duration.ofSeconds(30), null);

      // The CDN answered, just uselessly: that is the CDN's fault, not the network's.
      assertThrows(RegistryProtocolException.class, () -> fetcher.fetch(
          "http://127.0.0.1:" + server.getAddress().getPort() + "/hop", target));

      // Six: the first request plus the five redirects that are allowed to be taken.
      assertTrue(hops.get() <= 6, "followed " + hops.get() + " hops");
    } finally {
      Files.deleteIfExists(target);
      server.stop(0);
    }
  }

  @Test
  void downloadThatStallsFailsAsAnUnreachableHostRatherThanHangingTheCaller() throws IOException {
    try (var blackHole = new ServerSocket(0)) {
      var target = Files.createTempFile("fetcher-test-", ".jar");
      try {
        var fetcher = config("http://127.0.0.1:" + blackHole.getLocalPort())
            .marketplaceArtifactFetcher(Duration.ofSeconds(30), null);

        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
          var thrown = assertThrows(RegistryUnreachableException.class, () -> fetcher.fetch(
              "http://127.0.0.1:" + blackHole.getLocalPort() + "/artifact", target));
          assertEquals("127.0.0.1", thrown.getHost());
        });
      } finally {
        Files.deleteIfExists(target);
      }
    }
  }
}
