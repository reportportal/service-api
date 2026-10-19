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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.text.ParseException;

/**
 * Reads a licence token back the way a registry would: Nimbus parses the compact JWS, and the EdDSA signature is
 * checked with the JDK. Nimbus's own {@code Ed25519Verifier} needs the optional Tink dependency, which this service
 * does not have.
 */
public final class LicenceJwt {

  private LicenceJwt() {
  }

  /**
   * Parses a compact JWS, failing the calling test when it is not one.
   */
  public static SignedJWT parse(String token) {
    try {
      return SignedJWT.parse(token);
    } catch (ParseException e) {
      throw new AssertionError("not a compact JWS: " + token, e);
    }
  }

  /**
   * Whether the token's signature verifies under the given Ed25519 public key.
   */
  public static boolean verifies(String token, PublicKey publicKey) {
    var jwt = parse(token);
    try {
      var signature = Signature.getInstance("Ed25519");
      signature.initVerify(publicKey);
      signature.update(jwt.getSigningInput());
      return signature.verify(jwt.getSignature().decode());
    } catch (GeneralSecurityException _) {
      return false;
    }
  }

  /**
   * The token's claims, once its signature is proven to verify under the given public key.
   */
  public static JWTClaimsSet verifiedClaims(String token, PublicKey publicKey) {
    assertTrue(verifies(token, publicKey), "licence token signature does not verify");
    try {
      return parse(token).getJWTClaimsSet();
    } catch (ParseException e) {
      throw new AssertionError("licence token claims are not JSON", e);
    }
  }
}
