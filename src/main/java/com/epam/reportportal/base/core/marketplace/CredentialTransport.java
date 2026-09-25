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

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Whether a URL may carry a credential.
 *
 * <p>Two things this service sends are bearer credentials in their own right: the Ed25519 licence
 * JWT, and the signed download URL the registry answers a premium artifact request with. Over
 * cleartext either is readable by anything on the path, and a signed URL that leaks is an artifact
 * anyone can fetch until it expires.
 *
 * <p>The rule is not "https or nothing", because that would refuse to run on a loopback registry —
 * the shape every developer and every integration test uses, and one where there is no network to
 * read. Plaintext to loopback is allowed; plaintext anywhere else is refused unless an operator
 * turns it on deliberately with {@code marketplace.registry.allow-insecure-transport}, which exists
 * for an in-cluster registry reached over plain HTTP inside a trusted network.
 *
 * <p>What this deliberately does not govern is the download of a <em>public</em> artifact. No
 * credential is sent for one, its URL is not secret, and what it fetches is verified against the
 * SHA-256 the registry published before anything is installed — so plaintext there risks neither
 * disclosure nor a substituted jar.
 */
public final class CredentialTransport {

  private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");

  private final boolean allowInsecure;

  /**
   * Creates the policy this instance is configured for.
   *
   * @param allowInsecure whether plain HTTP to a non-loopback host may carry a credential, from
   *     {@code marketplace.registry.allow-insecure-transport}. False everywhere it is not set on
   *     purpose, which is the only setting safe on a path somebody else can watch.
   */
  public CredentialTransport(boolean allowInsecure) {
    this.allowInsecure = allowInsecure;
  }

  /**
   * Whether a credential may be sent to this URL.
   *
   * @param url an absolute URL; a blank or unparseable one is refused, because a credential is not
   *     something to send on a guess
   */
  public boolean permits(String url) {
    if (StringUtils.isBlank(url)) {
      return false;
    }
    URI uri;
    try {
      uri = URI.create(url.trim());
    } catch (IllegalArgumentException e) {
      return false;
    }
    if ("https".equalsIgnoreCase(uri.getScheme())) {
      return true;
    }
    if (!"http".equalsIgnoreCase(uri.getScheme())) {
      return false;
    }

    return allowInsecure || isLoopback(uri.getHost());
  }

  private static boolean isLoopback(String host) {
    return host != null && LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT));
  }

  /** The message every refusal shares: what was refused, and the one way to allow it. */
  public static String refusal(String what, String url) {
    return "Refusing to send " + what + " over an unencrypted connection to '" + url
        + "'. Use an https registry URL, or set"
        + " marketplace.registry.allow-insecure-transport=true if this is plain HTTP inside a"
        + " trusted network.";
  }
}
