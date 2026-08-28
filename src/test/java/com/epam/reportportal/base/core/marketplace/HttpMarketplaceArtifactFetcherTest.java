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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.epam.reportportal.base.core.configs.MarketplaceConfig;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * The artifact download: bytes to disk, redirects followed, a dead host still named.
 */
class HttpMarketplaceArtifactFetcherTest {

  private static final byte[] JAR = "PK pretend jar".getBytes(StandardCharsets.UTF_8);

  private static MarketplaceConfig config(String url) {
    return new MarketplaceConfig(url, Duration.ofSeconds(3), Duration.ofSeconds(5),
        Duration.ofSeconds(30));
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
          .marketplaceArtifactFetcher(Duration.ofSeconds(30));

      fetcher.fetch("http://127.0.0.1:" + server.getAddress().getPort() + "/artifact", target);

      assertArrayEquals(JAR, Files.readAllBytes(target));
    } finally {
      Files.deleteIfExists(target);
      server.stop(0);
    }
  }

  @Test
  void aDownloadThatStallsFailsAsAnUnreachableHostRatherThanHangingTheCaller() throws IOException {
    try (var blackHole = new ServerSocket(0)) {
      var target = Files.createTempFile("fetcher-test-", ".jar");
      try {
        var fetcher = config("http://127.0.0.1:" + blackHole.getLocalPort())
            .marketplaceArtifactFetcher(Duration.ofSeconds(30));

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
