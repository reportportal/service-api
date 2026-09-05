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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The licence JWT, checked against what the registry actually reads: EdDSA over the claims
 * {@code customerId} and {@code pluginId} with {@code iat} and {@code exp}, verified by the
 * entitlement's public key (see the registry's {@code auth.VerifyLicenseJWT}).
 *
 * <p>Tokens are parsed back with jjwt rather than by this code's own reader — an assembler that
 * agrees only with itself proves nothing about a token a Go registry has to accept.
 */
class Ed25519MarketplaceLicenceTest {

  private static final String CUSTOMER = "acme-gmbh";
  private static final String PLUGIN_ID = "jira";
  private static final Instant NOW = Instant.parse("2026-07-29T10:15:30Z");

  private KeyPair keyPair;
  private StubStore store;

  @BeforeEach
  void setUp() throws Exception {
    keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    store = new StubStore();
  }

  /**
   * The registry hands out the key as Go prints it: base64 of {@code ed25519.PrivateKey}, which is
   * the 32-byte seed followed by the 32-byte public key.
   */
  private String goStylePrivateKey() {
    var pkcs8 = keyPair.getPrivate().getEncoded();
    var seed = java.util.Arrays.copyOfRange(pkcs8, pkcs8.length - 32, pkcs8.length);
    var x509 = keyPair.getPublic().getEncoded();
    var raw = new byte[64];
    System.arraycopy(seed, 0, raw, 0, 32);
    System.arraycopy(x509, x509.length - 32, raw, 32, 32);
    return Base64.getEncoder().encodeToString(raw);
  }

  private String seedOnlyPrivateKey() {
    var pkcs8 = keyPair.getPrivate().getEncoded();
    return Base64.getEncoder()
        .encodeToString(java.util.Arrays.copyOfRange(pkcs8, pkcs8.length - 32, pkcs8.length));
  }

  private Ed25519MarketplaceLicence licence() {
    return licence(Duration.ofSeconds(60), NOW);
  }

  private Ed25519MarketplaceLicence licence(Duration ttl, Instant at) {
    return new Ed25519MarketplaceLicence(store, ttl, Clock.fixed(at, ZoneOffset.UTC));
  }

  private io.jsonwebtoken.Claims claims(String token, PublicKey publicKey) {
    return Jwts.parser()
        .verifyWith(publicKey)
        .clockSkewSeconds(0)
        .clock(() -> java.util.Date.from(NOW))
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  @Test
  void anInstanceWithoutCredentialsIsNotConfiguredAndSignsNothing() {
    assertFalse(licence().isConfigured());
    assertTrue(licence().signArtifactToken(PLUGIN_ID).isEmpty());
  }

  @Test
  void anInstanceWithCredentialsIsConfigured() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    assertTrue(licence().isConfigured());
  }

  /** {@link MarketplaceLicence#isConfigured()} must not need the private key to answer. */
  @Test
  void beingConfiguredIsAnsweredWithoutDecryptingTheKey() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    assertTrue(licence().isConfigured());
    assertEquals(0, store.credentialReads);
  }

  @Test
  void theTokenCarriesTheClaimsTheRegistryReadsAndVerifiesWithTheMatchingPublicKey() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    var token = licence().signArtifactToken(PLUGIN_ID).orElseThrow();

    var claims = claims(token, keyPair.getPublic());
    assertEquals(CUSTOMER, claims.get("customerId"));
    assertEquals(PLUGIN_ID, claims.get("pluginId"));
    assertEquals(NOW.getEpochSecond(), claims.getExpiration().toInstant().getEpochSecond() - 60);
    assertTrue(claims.getIssuedAt().toInstant().isBefore(claims.getExpiration().toInstant()));
  }

  @Test
  void theHeaderNamesEdDsaSoTheRegistryPicksTheRightAlgorithm() throws Exception {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    var token = licence().signArtifactToken(PLUGIN_ID).orElseThrow();

    var header = new ObjectMapper().readTree(
        Base64.getUrlDecoder().decode(token.substring(0, token.indexOf('.'))));
    assertEquals("EdDSA", header.get("alg").asText());
    assertEquals("JWT", header.get("typ").asText());
  }

  @Test
  void aSeedOnlyPrivateKeyIsAcceptedToo() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, seedOnlyPrivateKey());

    var token = licence().signArtifactToken(PLUGIN_ID).orElseThrow();

    assertEquals(CUSTOMER, claims(token, keyPair.getPublic()).get("customerId"));
  }

  @Test
  void aTokenSignedForOneCustomerDoesNotVerifyUnderAnotherKey() throws Exception {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());
    var somebodyElse = KeyPairGenerator.getInstance("Ed25519").generateKeyPair().getPublic();

    var token = licence().signArtifactToken(PLUGIN_ID).orElseThrow();

    assertThrows(io.jsonwebtoken.security.SignatureException.class,
        () -> claims(token, somebodyElse));
  }

  /**
   * The registry's own signed URL lives about 60 seconds and the token is used once, immediately.
   */
  @Test
  void theTokenLifetimeIsNoLongerThanTheDownloadItAuthorises() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    var claims = claims(licence().signArtifactToken(PLUGIN_ID).orElseThrow(), keyPair.getPublic());

    var lifetime = Duration.between(claims.getIssuedAt().toInstant(),
        claims.getExpiration().toInstant());
    assertTrue(lifetime.compareTo(Duration.ofMinutes(2)) <= 0, "lifetime was " + lifetime);
  }

  /**
   * The registry (jwx) rejects an {@code iat} in the future with no skew allowance, so an instance
   * whose clock runs a little fast must still be able to sign.
   */
  @Test
  void issuedAtIsBackdatedSoASlightlyFastClockStillSigns() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    var claims = claims(licence().signArtifactToken(PLUGIN_ID).orElseThrow(), keyPair.getPublic());

    assertTrue(claims.getIssuedAt().toInstant().isBefore(NOW),
        "iat was " + claims.getIssuedAt().toInstant());
  }

  /** One instance, one plugin, a clock that moved: a cached token would come back unchanged. */
  @Test
  void everyCallSignsAFreshTokenRatherThanReplayingTheLastOne() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());
    var now = new java.util.concurrent.atomic.AtomicReference<>(NOW);
    var licence = new Ed25519MarketplaceLicence(store, Duration.ofSeconds(60), new Clock() {
      @Override
      public java.time.ZoneId getZone() {
        return ZoneOffset.UTC;
      }

      @Override
      public Clock withZone(java.time.ZoneId zone) {
        return this;
      }

      @Override
      public Instant instant() {
        return now.get();
      }
    });

    var first = licence.signArtifactToken(PLUGIN_ID).orElseThrow();
    now.set(NOW.plusSeconds(5));
    var later = licence.signArtifactToken(PLUGIN_ID).orElseThrow();

    assertNotEquals(first, later);
  }

  @Test
  void aTokenIsScopedToTheOnePluginItWasAskedFor() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER, goStylePrivateKey());

    var token = licence().signArtifactToken("rally").orElseThrow();

    assertEquals("rally", claims(token, keyPair.getPublic()).get("pluginId"));
  }

  @Test
  void aStoredKeyThatIsNotAnEd25519KeyIsReportedRatherThanSigningNonsense() {
    store.credentials = new MarketplaceLicenceCredentials(CUSTOMER,
        Base64.getEncoder().encodeToString("far too short".getBytes(UTF_8)));

    assertThrows(ReportPortalException.class, () -> licence().signArtifactToken(PLUGIN_ID));
  }

  /** Counts credential reads so "configured" cannot quietly start decrypting. */
  private static final class StubStore implements MarketplaceLicenceStore {

    private MarketplaceLicenceCredentials credentials;
    private int credentialReads;

    @Override
    public Optional<String> customerId() {
      return Optional.ofNullable(credentials).map(MarketplaceLicenceCredentials::customerId);
    }

    @Override
    public Optional<MarketplaceLicenceCredentials> credentials() {
      credentialReads++;
      return Optional.ofNullable(credentials);
    }

    @Override
    public void save(String customerId, String privateKey) {
      credentials = new MarketplaceLicenceCredentials(customerId, privateKey);
    }

    @Override
    public void clear() {
      credentials = null;
    }
  }
}
