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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Licence credentials in {@code server_settings}, encrypted with the same
 * {@link BasicTextEncryptor} integration credentials already use.
 */
class ServerSettingsMarketplaceLicenceStoreTest {

  private static final String CUSTOMER = "acme-gmbh";
  private static final String PRIVATE_KEY =
      "3sBz1kL1YkKcW5m6bqXo8t0kQmHb8bYxfF0oQ0v1cGZ2Uq2rV9d0oQ5j3nGkD0Ff4dQ0KpWq1sJ8xZlA9dTgQg==";

  private final Map<String, ServerSettings> rows = new HashMap<>();
  private ServerSettingsRepository repository;
  private BasicTextEncryptor encryptor;
  private ServerSettingsMarketplaceLicenceStore store;

  @BeforeEach
  void setUp() {
    rows.clear();
    repository = mock(ServerSettingsRepository.class);
    when(repository.findByKey(anyString()))
        .thenAnswer(invocation -> Optional.ofNullable(rows.get(invocation.getArgument(0))));
    when(repository.save(any(ServerSettings.class))).thenAnswer(invocation -> {
      var saved = invocation.getArgument(0, ServerSettings.class);
      rows.put(saved.getKey(), saved);
      return saved;
    });
    encryptor = encryptorWith("the-instance-secret");
    store = new ServerSettingsMarketplaceLicenceStore(repository, encryptor);
  }

  private static BasicTextEncryptor encryptorWith(String password) {
    var textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPassword(password);
    return textEncryptor;
  }

  @Test
  void credentialsSurviveTheRoundTrip() {
    store.save(CUSTOMER, PRIVATE_KEY);

    var loaded = store.credentials().orElseThrow();
    assertEquals(CUSTOMER, loaded.customerId());
    assertEquals(PRIVATE_KEY, loaded.privateKey());
    assertEquals(Optional.of(CUSTOMER), store.customerId());
  }

  @Test
  void anInstanceThatWasNeverConfiguredHoldsNothing() {
    assertTrue(store.credentials().isEmpty());
    assertTrue(store.customerId().isEmpty());
  }

  @Test
  void savingAgainReplacesWhatWasThere() {
    store.save(CUSTOMER, PRIVATE_KEY);
    store.save("other-gmbh", "b3RoZXIta2V5");

    var loaded = store.credentials().orElseThrow();
    assertEquals("other-gmbh", loaded.customerId());
    assertEquals("b3RoZXIta2V5", loaded.privateKey());
  }

  @Test
  void thePrivateKeyIsEncryptedAtRest() {
    store.save(CUSTOMER, PRIVATE_KEY);

    var persisted = rows.get(ServerSettingsMarketplaceLicenceStore.PRIVATE_KEY_KEY).getValue();
    assertNotEquals(PRIVATE_KEY, persisted);
    assertFalse(persisted.contains(PRIVATE_KEY.substring(0, 16)), persisted);
    // Same secret, independent encryptor: the row is real ciphertext, not an unrelated blob.
    assertEquals(PRIVATE_KEY, encryptorWith("the-instance-secret").decrypt(persisted));
  }

  @Test
  void credentialsThatCannotBeDecryptedAreReportedRatherThanReturnedAsGarbage() {
    store.save(CUSTOMER, PRIVATE_KEY);
    var afterPasswordChange =
        new ServerSettingsMarketplaceLicenceStore(repository, encryptorWith("a-different-secret"));

    var thrown = assertThrows(ReportPortalException.class, afterPasswordChange::credentials);
    assertEquals(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED, thrown.getErrorType());
  }

  /**
   * {@code ServerSettingsRepositoryCustomImpl.selectServerSettings} — what {@code GET /settings}
   * renders — returns every row whose key starts with {@code server.}. Licence rows must not be in
   * that answer at all, encrypted or not, so they are keyed outside the prefix.
   */
  @Test
  void licenceRowsAreOutsideTheKeyPrefixTheSettingsEndpointReturns() {
    store.save(CUSTOMER, PRIVATE_KEY);

    assertFalse(rows.isEmpty());
    rows.keySet().forEach(key -> assertFalse(key.startsWith("server."), key));
  }
}
