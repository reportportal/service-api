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

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Reads the Ed25519 private key the registry issued.
 *
 * <p>It is base64 of Go's {@code ed25519.PrivateKey} — 32 bytes of seed followed by the 32-byte
 * public key. The bare 32-byte seed is accepted as well, because that is what other Ed25519
 * tooling prints and an operator who pastes one should be told so at the moment they paste it,
 * rather than by a premium install failing later.
 *
 * <p>Throws {@link IllegalArgumentException} rather than a typed failure: the same unusable key is
 * a bad request when an admin submits it and a broken instance when it is already stored, and the
 * two callers say so in their own words.
 */
public final class Ed25519LicenceKey {

  private static final int SEED_LENGTH = 32;

  private Ed25519LicenceKey() {
  }

  /**
   * Parses the key.
   *
   * @param base64 base64 Ed25519 private key, 32 or 64 bytes decoded
   * @return the key, ready to sign with
   * @throws IllegalArgumentException when it is not one
   */
  public static PrivateKey privateKey(String base64) {
    if (base64 == null || base64.isBlank()) {
      throw new IllegalArgumentException("the licence private key is empty");
    }
    byte[] raw;
    try {
      raw = Base64.getDecoder().decode(base64.trim());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("the licence private key is not base64");
    }
    if (raw.length != SEED_LENGTH && raw.length != 2 * SEED_LENGTH) {
      Arrays.fill(raw, (byte) 0);
      throw new IllegalArgumentException(
          "the licence private key decodes to " + raw.length + " bytes; an Ed25519 private key is "
              + SEED_LENGTH + " or " + 2 * SEED_LENGTH);
    }
    var seed = Arrays.copyOf(raw, SEED_LENGTH);
    try {
      return KeyFactory.getInstance("Ed25519")
          .generatePrivate(new EdECPrivateKeySpec(NamedParameterSpec.ED25519, seed));
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException("the licence private key is not a usable Ed25519 key");
    } finally {
      Arrays.fill(seed, (byte) 0);
      Arrays.fill(raw, (byte) 0);
    }
  }
}
