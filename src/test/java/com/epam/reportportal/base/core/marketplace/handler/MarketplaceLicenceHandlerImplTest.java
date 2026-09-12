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

package com.epam.reportportal.base.core.marketplace.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.marketplace.MarketplaceLicenceCredentials;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicenceStore;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceRQ;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Setting and inspecting licence credentials. */
class MarketplaceLicenceHandlerImplTest {

  private final StubStore store = new StubStore();
  private ReportPortalUser user;
  private MarketplaceLicenceHandlerImpl handler;
  private String privateKey;

  @BeforeEach
  void setUp() throws Exception {
    user = mock(ReportPortalUser.class);
    when(user.getUsername()).thenReturn("admin");
    handler = new MarketplaceLicenceHandlerImpl(store);
    var pkcs8 = KeyPairGenerator.getInstance("Ed25519").generateKeyPair().getPrivate().getEncoded();
    privateKey = Base64.getEncoder()
        .encodeToString(Arrays.copyOfRange(pkcs8, pkcs8.length - 32, pkcs8.length));
  }

  private static KeyPair keyPair() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  /** Go prints the key as the 32-byte seed followed by the 32-byte public key. */
  private static String goStyleKey(KeyPair seedFrom, KeyPair publicHalfFrom) {
    var pkcs8 = seedFrom.getPrivate().getEncoded();
    var x509 = publicHalfFrom.getPublic().getEncoded();
    var raw = new byte[64];
    System.arraycopy(pkcs8, pkcs8.length - 32, raw, 0, 32);
    System.arraycopy(x509, x509.length - 32, raw, 32, 32);
    return Base64.getEncoder().encodeToString(raw);
  }

  @Test
  void anInstanceWithoutCredentialsReportsThatAndNoCustomer() {
    var resource = handler.getLicence();

    assertFalse(resource.configured());
    assertNull(resource.customerId());
  }

  @Test
  void settingCredentialsStoresThemAndReportsTheCustomer() {
    var resource = handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", privateKey), user);

    assertTrue(resource.configured());
    assertEquals("acme-gmbh", resource.customerId());
    assertEquals("acme-gmbh", store.held.customerId());
    assertEquals(privateKey, store.held.privateKey());
    assertTrue(handler.getLicence().configured());
  }

  @Test
  void surroundingWhitespaceFromACopyPasteIsNotStored() {
    handler.setLicence(new MarketplaceLicenceRQ("  acme-gmbh ", " " + privateKey + "\n"), user);

    assertEquals("acme-gmbh", store.held.customerId());
    assertEquals(privateKey, store.held.privateKey());
  }

  /**
   * A key that cannot sign is rejected at the moment it is submitted. Storing it would turn one
   * admin's typo into a premium install failing days later at the registry, which cannot say why.
   */
  @Test
  void aKeyOfTheWrongLengthIsRefusedAndNothingIsStored() {
    var thrown = assertThrows(ReportPortalException.class, () -> handler.setLicence(
        new MarketplaceLicenceRQ("acme-gmbh", Base64.getEncoder().encodeToString(new byte[7])),
        user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    assertNull(store.held);
  }

  @Test
  void aKeyThatIsNotBase64IsRefusedAndNothingIsStored() {
    var thrown = assertThrows(ReportPortalException.class,
        () -> handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", "not base64 at all!"),
            user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    assertNull(store.held);
  }

  /**
   * The operator who pasted the wrong key, or whose entitlement ended, gets the instance back to
   * holding nothing — which is the only honest state for one that cannot use premium plugins.
   */
  @Test
  void deletingCredentialsLeavesTheInstanceHoldingNothing() {
    handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", privateKey), user);

    var resource = handler.deleteLicence(user);

    assertFalse(resource.configured());
    assertNull(resource.customerId());
    assertNull(store.held);
    assertFalse(handler.getLicence().configured());
  }

  /** Deleting what is not there is the state being asked for, so it is not a failure. */
  @Test
  void deletingWhenNothingIsConfiguredIsNotAnError() {
    var resource = handler.deleteLicence(user);

    assertFalse(resource.configured());
    assertTrue(store.cleared);
  }

  /** The 64-byte form the registry hands out, with both halves from the same key pair. */
  @Test
  void aGoStyleKeyWhoseHalvesBelongTogetherIsStored() throws Exception {
    var pair = keyPair();

    handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", goStyleKey(pair, pair)), user);

    assertEquals(goStyleKey(pair, pair), store.held.privateKey());
  }

  /**
   * A key spliced from two pastes is still 64 base64 bytes, so length alone accepts it. The next
   * seam that would notice is the registry's 403, which cannot say a key is malformed — this is
   * the only place the operator can be told.
   */
  @Test
  void aKeyWhosePublicHalfDoesNotMatchItsSeedIsRefusedAsMalformed() throws Exception {
    var spliced = goStyleKey(keyPair(), keyPair());

    var thrown = assertThrows(ReportPortalException.class,
        () -> handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", spliced), user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("malformed"), thrown.getMessage());
    assertNull(store.held);
  }

  /** A public half that is not a curve point at all is the same bad request, not a 500. */
  @Test
  void aCorruptedPublicHalfIsRefusedRatherThanEscapingAsAServerError() throws Exception {
    var raw = Base64.getDecoder().decode(goStyleKey(keyPair(), keyPair()));
    Arrays.fill(raw, 32, 64, (byte) 0xFF);
    var corrupted = Base64.getEncoder().encodeToString(raw);

    var thrown = assertThrows(ReportPortalException.class,
        () -> handler.setLicence(new MarketplaceLicenceRQ("acme-gmbh", corrupted), user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("malformed"), thrown.getMessage());
    assertNull(store.held);
  }

  private static final class StubStore implements MarketplaceLicenceStore {

    private MarketplaceLicenceCredentials held;
    private boolean cleared;

    @Override
    public Optional<String> customerId() {
      return Optional.ofNullable(held).map(MarketplaceLicenceCredentials::customerId);
    }

    @Override
    public Optional<MarketplaceLicenceCredentials> credentials() {
      return Optional.ofNullable(held);
    }

    @Override
    public void save(String customerId, String privateKey) {
      held = new MarketplaceLicenceCredentials(customerId, privateKey);
    }

    @Override
    public void clear() {
      held = null;
      cleared = true;
    }
  }
}
