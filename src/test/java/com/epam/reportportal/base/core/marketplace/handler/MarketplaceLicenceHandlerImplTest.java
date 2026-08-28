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

  private static final class StubStore implements MarketplaceLicenceStore {

    private MarketplaceLicenceCredentials held;

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
  }
}
