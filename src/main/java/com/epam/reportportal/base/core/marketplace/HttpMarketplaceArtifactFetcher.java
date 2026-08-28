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

import com.epam.reportportal.base.core.marketplace.exception.RegistryProtocolException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Streams the artifact straight to disk. It is never held in memory: a plugin jar is tens of
 * megabytes and several concurrent installs would be paid for by the whole service.
 */
public class HttpMarketplaceArtifactFetcher implements MarketplaceArtifactFetcher {

  private final RestTemplate restTemplate;

  public HttpMarketplaceArtifactFetcher(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void fetch(String downloadUrl, Path target) {
    URI uri;
    try {
      uri = URI.create(downloadUrl);
    } catch (IllegalArgumentException e) {
      throw new RegistryProtocolException(
          "Marketplace registry returned an unusable download URL", e);
    }
    try {
      restTemplate.execute(uri, HttpMethod.GET, null, response -> {
        Files.copy(response.getBody(), target, StandardCopyOption.REPLACE_EXISTING);
        return null;
      });
    } catch (ResourceAccessException e) {
      throw new RegistryUnreachableException(host(uri), e);
    } catch (RestClientException e) {
      var timeout = timeoutCause(e);
      if (timeout != null) {
        throw new RegistryUnreachableException(host(uri), timeout);
      }
      throw new RegistryProtocolException(
          "Marketplace artifact could not be downloaded from '" + host(uri) + "'", e);
    }
  }

  private static String host(URI uri) {
    return Optional.ofNullable(uri.getHost()).orElse(uri.toString());
  }

  /**
   * A stall part-way through a body surfaces wrapped by whatever was reading it; the download is
   * still a registry that went quiet, not a broken artifact.
   */
  private static Throwable timeoutCause(Throwable e) {
    for (var cause = e; cause != null; cause = cause.getCause()) {
      if (cause instanceof InterruptedIOException) {
        return cause;
      }
    }
    return null;
  }
}
