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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Signs the licence JWT the registry's premium artifact route asks for: EdDSA over
 * {@code customerId}, {@code pluginId}, {@code iat} and {@code exp} — the four the registry reads
 * (its {@code auth.PeekUnverifiedCustomerID} finds the entitlement by {@code customerId}, then
 * {@code auth.VerifyLicenseJWT} checks the signature against that entitlement's public keys and
 * the handler compares {@code pluginId}).
 *
 * <p>No JWT library is added for this. Ed25519 is in the JDK itself from 15 and this service
 * builds on 25; the nimbus-jose-jwt 9.47 already on the classpath could not have done it anyway,
 * since its {@code Ed25519Signer} needs the optional Tink dependency, which is not a dependency
 * here. What is left is a compact JWS, which is two base64url segments and a signature over them.
 *
 * <p>A token is signed at the moment of use and never cached. It is worth nothing to anyone
 * holding it a minute later, which is the point.
 */
@Component
public class Ed25519MarketplaceLicence implements MarketplaceLicence {

  private static final ObjectMapper JSON = JsonMapper.builder().build();
  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  /** {@code {"alg":"EdDSA","typ":"JWT"}}, the only header this ever sends. */
  private static final String HEADER =
      B64.encodeToString("{\"alg\":\"EdDSA\",\"typ\":\"JWT\"}".getBytes(UTF_8));
  /**
   * The registry validates {@code iat} with no skew allowance of its own (jwx's
   * {@code isIssuedAtValid}), so an instance whose clock runs a second fast would sign tokens the
   * registry rejects as issued in the future. Backdating by this much costs nothing — {@code exp}
   * still bounds the token — and buys the only clock agreement the two ends need.
   */
  private static final Duration CLOCK_SKEW_ALLOWANCE = Duration.ofSeconds(30);

  private final MarketplaceLicenceStore store;
  private final Duration tokenTtl;
  private final Clock clock;

  /**
   * Creates the licence.
   *
   * @param store    where the credentials live
   * @param tokenTtl how long a signed token stays valid
   */
  @Autowired
  public Ed25519MarketplaceLicence(MarketplaceLicenceStore store,
      @Value("${marketplace.licence.token-ttl:PT60S}") Duration tokenTtl) {
    this(store, tokenTtl, Clock.systemUTC());
  }

  /**
   * As above, with the clock the token's {@code iat} and {@code exp} are read from — a test can
   * sign at a known instant without waiting for one.
   *
   * @param store    where the credentials live
   * @param tokenTtl how long a signed token stays valid
   * @param clock    the clock to read
   */
  public Ed25519MarketplaceLicence(MarketplaceLicenceStore store, Duration tokenTtl, Clock clock) {
    this.store = store;
    this.tokenTtl = tokenTtl;
    this.clock = clock;
  }

  /** Answered from the customer id alone: a locked badge is not worth decrypting a key for. */
  @Override
  public boolean isConfigured() {
    return store.customerId().isPresent();
  }

  @Override
  public Optional<String> signArtifactToken(String pluginId) {
    return store.credentials().map(credentials -> sign(credentials, pluginId));
  }

  private String sign(MarketplaceLicenceCredentials credentials, String pluginId) {
    var now = clock.instant();
    var claims = new LinkedHashMap<String, Object>();
    claims.put("customerId", credentials.customerId());
    claims.put("pluginId", pluginId);
    claims.put("iat", now.minus(CLOCK_SKEW_ALLOWANCE).getEpochSecond());
    claims.put("exp", now.plus(tokenTtl).getEpochSecond());

    var signingInput = HEADER + "." + B64.encodeToString(json(claims));
    return signingInput + "." + B64.encodeToString(
        signature(privateKey(credentials.privateKey()), signingInput.getBytes(US_ASCII)));
  }

  private static byte[] json(Object claims) {
    try {
      return JSON.writeValueAsBytes(claims);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("licence claims could not be serialised", e);
    }
  }

  /** A stored key that will not parse is this instance being broken, not the caller being wrong. */
  private static PrivateKey privateKey(String base64) {
    try {
      return Ed25519LicenceKey.privateKey(base64);
    } catch (IllegalArgumentException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED,
          e.getMessage() + "; set the licence credentials again");
    }
  }

  private static byte[] signature(PrivateKey key, byte[] signingInput) {
    try {
      var signature = Signature.getInstance("Ed25519");
      signature.initSign(key);
      signature.update(signingInput);
      return signature.sign();
    } catch (GeneralSecurityException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED,
          "the licence token could not be signed: " + e.getMessage());
    }
  }
}
