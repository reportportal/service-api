/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.file;

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Creates and verifies short-lived HMAC signatures for public attachment stream URLs.
 */
@Component
public class FileSignedLinkService {

  private static final String CANONICAL_VERSION = "v1";
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String SECRET_KEY = "secret.key";
  private static final String STREAM_KEY_CONTEXT = "rp-attachment-stream-v1";

  private final ServerSettingsRepository serverSettingsRepository;
  private final String signingKey;
  private final Duration ttl;
  private final Clock clock;

  private byte[] streamKey;

  /**
   * Creates the token service.
   *
   * @param serverSettingsRepository server settings repository
   * @param signingKey               configured JWT signing key
   * @param ttl                      public stream URL lifetime
   */
  @Autowired
  public FileSignedLinkService(ServerSettingsRepository serverSettingsRepository,
      @Value("${rp.jwt.signing-key:}") String signingKey,
      @Value("${rp.attachment.public-stream.ttl:PT15M}") Duration ttl) {
    this(serverSettingsRepository, signingKey, ttl, Clock.systemUTC());
  }

  public FileSignedLinkService(ServerSettingsRepository serverSettingsRepository, String signingKey,
      Duration ttl, Clock clock) {
    this.serverSettingsRepository = serverSettingsRepository;
    this.signingKey = signingKey;
    this.ttl = ttl;
    this.clock = clock;
  }

  /**
   * Derives a feature-specific key from the shared master secret.
   */
  @PostConstruct
  void initialize() {
    streamKey = hmac(resolveMasterSecret().getBytes(StandardCharsets.UTF_8), STREAM_KEY_CONTEXT);
  }

  /**
   * Signs a public attachment stream link.
   *
   * @param attachmentId attachment ID
   * @param projectKey   project key
   * @param userId       issuing user ID
   * @return signature and expiration time
   */
  public SignedPublicLink createLink(long attachmentId, String projectKey, long userId) {
    long expiresAt = Instant.now(clock).plus(ttl).getEpochSecond();
    return new SignedPublicLink(createSignature(attachmentId, projectKey, userId, expiresAt),
        expiresAt);
  }

  /**
   * Verifies a public attachment stream token.
   *
   * @param attachmentId attachment ID from the path
   * @param projectKey   project key from the token
   * @param userId       issuing user ID from the token
   * @param exp          expiration epoch seconds
   * @param signature    HMAC signature
   * @return verified token payload
   * @throws BadCredentialsException when any token part is invalid
   */
  public StreamTokenPayload verify(long attachmentId, String projectKey, Long userId, Long exp,
      String signature) {

    if (!StringUtils.hasText(projectKey) || userId == null || exp == null || !StringUtils.hasText(
        signature) || exp <= Instant.now(clock).getEpochSecond()) {
      throw invalidToken();
    }

    var expectedSignature = createSignature(attachmentId, projectKey, userId, exp);

    if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.US_ASCII),
        signature.getBytes(StandardCharsets.US_ASCII))) {
      throw invalidToken();
    }

    return new StreamTokenPayload(attachmentId, projectKey, userId, exp);
  }

  private String resolveMasterSecret() {
    if (StringUtils.hasText(signingKey)) {
      return signingKey;
    }
    return serverSettingsRepository.findByKey(SECRET_KEY).map(ServerSettings::getValue)
        .orElseGet(serverSettingsRepository::generateSecret);
  }

  private String createSignature(long attachmentId, String projectKey, long userId,
      long expiresAt) {
    var canonical = String.join("\n", CANONICAL_VERSION, Long.toString(attachmentId), projectKey,
        Long.toString(userId), Long.toString(expiresAt));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(streamKey, canonical));
  }

  private byte[] hmac(byte[] key, String value) {
    try {
      var mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
      return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
      throw new IllegalStateException("Unable to create attachment stream signature", exception);
    }
  }

  private BadCredentialsException invalidToken() {
    return new BadCredentialsException("Invalid or expired attachment stream token");
  }

  /**
   * Result of signing a stream link.
   *
   * @param signature            URL-safe signature
   * @param expiresAtEpochSecond expiration epoch seconds
   */
  public record SignedPublicLink(String signature, long expiresAtEpochSecond) {

  }

  /**
   * Verified stream token fields.
   *
   * @param attachmentId         attachment ID
   * @param projectKey           project key
   * @param userId               issuing user ID
   * @param expiresAtEpochSecond expiration epoch seconds
   */
  public record StreamTokenPayload(long attachmentId, String projectKey, long userId,
                                   long expiresAtEpochSecond) {

  }
}
