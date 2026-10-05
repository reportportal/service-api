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
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.apache.hc.client5.http.ClientProtocolException;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Streams the artifact straight to disk. It is never held in memory: a plugin jar is tens of
 * megabytes and several concurrent installs would be paid for by the whole service.
 */
public class HttpMarketplaceArtifactFetcher implements MarketplaceArtifactFetcher {

  /**
   * What the registry itself refuses to accept: a publish bundle over 160 MiB is rejected at
   * upload (registry {@code handlers_plugins.go}, {@code http.MaxBytesReader(nil, r.Body,
   * 160&lt;&lt;20)}), so nothing larger can have been published and anything larger arriving here
   * did not come from a registry behaving as one.
   */
  public static final long DEFAULT_MAX_ARTIFACT_BYTES = 160L << 20;

  private static final int BUFFER_BYTES = 8192;

  private final RestTemplate restTemplate;
  private final long maxArtifactBytes;

  /**
   * A fetcher bounded by what the registry itself will publish.
   *
   * @param restTemplate the download template, with its own longer deadline
   */
  public HttpMarketplaceArtifactFetcher(RestTemplate restTemplate) {
    this(restTemplate, DEFAULT_MAX_ARTIFACT_BYTES);
  }

  /**
   * Creates a fetcher.
   *
   * @param restTemplate     the download template, with its own longer deadline
   * @param maxArtifactBytes the most that may be written to disk for one artifact
   */
  public HttpMarketplaceArtifactFetcher(RestTemplate restTemplate, long maxArtifactBytes) {
    this.restTemplate = restTemplate;
    this.maxArtifactBytes = maxArtifactBytes;
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
        copyBounded(response.getBody(), target, host(uri));
        return null;
      });
    } catch (ResourceAccessException e) {
      // A protocol violation — a redirect chain past the bound, a malformed answer — arrives here
      // wrapped as an I/O failure. The host answered, so it is not the one to report as down.
      if (timeoutCause(e) == null && causeOfType(e, ClientProtocolException.class) != null) {
        throw new RegistryProtocolException(
            "Marketplace artifact could not be downloaded from '" + host(uri) + "'", e);
      }
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

  /**
   * Copies the body to disk, refusing to write more than the bound.
   *
   * <p>The checksum that would catch a substituted artifact is verified only once the whole body
   * is on disk, and the download follows redirects, so the host that answers last is not
   * necessarily the registry. A response that streams for as long as the deadline allows could
   * therefore fill the filesystem before anything had a chance to reject it — and a full
   * filesystem is not one failed install, it is every request the service is serving.
   *
   * <p>A partial file is removed on the way out. Leaving one behind would hand the install path a
   * truncated jar whose only defence is the checksum, and would keep the space that was the
   * problem in the first place.
   */
  private void copyBounded(InputStream body, Path target, String host) throws IOException {
    var written = 0L;
    try (OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
      var buffer = new byte[BUFFER_BYTES];
      for (var read = body.read(buffer); read >= 0; read = body.read(buffer)) {
        written += read;
        if (written > maxArtifactBytes) {
          throw new ArtifactTooLargeException(host, maxArtifactBytes);
        }
        out.write(buffer, 0, read);
      }
    } catch (IOException | RuntimeException e) {
      Files.deleteIfExists(target);
      throw e;
    }
  }

  /** Thrown through the response extractor; {@code fetch} lets it past its own catches. */
  private static final class ArtifactTooLargeException extends RegistryProtocolException {

    private ArtifactTooLargeException(String host, long limit) {
      super("Marketplace artifact from '" + host + "' exceeds the maximum download size of "
          + limit + " bytes and was not written to disk");
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
    return causeOfType(e, InterruptedIOException.class);
  }

  private static Throwable causeOfType(Throwable e, Class<? extends Throwable> type) {
    for (var cause = e; cause != null; cause = cause.getCause()) {
      if (type.isInstance(cause)) {
        return cause;
      }
    }
    return null;
  }
}
