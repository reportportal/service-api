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

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Licence credentials in the {@code server_settings} table, with the private key encrypted by the
 * same {@link BasicTextEncryptor} that integration credentials use — the instance secret from
 * {@code rp.encryptor.password} or the generated {@code secret-integration-salt}. Nothing new is
 * invented here: one more thing encrypted by the one mechanism this service already has.
 *
 * <p>The keys deliberately start with {@code marketplace.} rather than {@code server.}.
 * {@code ServerSettingsRepositoryCustomImpl.selectServerSettings} — the query behind the settings
 * endpoint — returns every {@code server.%} row, and a licence key has no business being in a
 * response that renders instance settings, ciphertext or not.
 */
@Component
public class ServerSettingsMarketplaceLicenceStore implements MarketplaceLicenceStore {

  static final String CUSTOMER_ID_KEY = "marketplace.licence.customerId";
  static final String PRIVATE_KEY_KEY = "marketplace.licence.privateKey";

  private final ServerSettingsRepository repository;
  private final BasicTextEncryptor encryptor;

  /**
   * Creates the store.
   *
   * @param repository server settings rows
   * @param encryptor  the instance-wide credential encryptor
   */
  public ServerSettingsMarketplaceLicenceStore(ServerSettingsRepository repository,
      BasicTextEncryptor encryptor) {
    this.repository = repository;
    this.encryptor = encryptor;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> customerId() {
    return value(CUSTOMER_ID_KEY);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MarketplaceLicenceCredentials> credentials() {
    var customerId = value(CUSTOMER_ID_KEY);
    var stored = value(PRIVATE_KEY_KEY);
    if (customerId.isEmpty() || stored.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new MarketplaceLicenceCredentials(customerId.get(), decrypt(stored.get())));
  }

  /** Both rows in one transaction: a half-written credential would read as configured. */
  @Override
  @Transactional
  public void save(String customerId, String privateKey) {
    write(CUSTOMER_ID_KEY, customerId);
    write(PRIVATE_KEY_KEY, encryptor.encrypt(privateKey));
  }

  /**
   * The rows are deleted, not blanked: a key an operator has removed should not stay in the table
   * as ciphertext waiting for the instance secret to leak. Both go in one transaction, for the
   * reason {@link #save} writes both in one.
   */
  @Override
  @Transactional
  public void clear() {
    remove(CUSTOMER_ID_KEY);
    remove(PRIVATE_KEY_KEY);
  }

  private Optional<String> value(String key) {
    return repository.findByKey(key).map(ServerSettings::getValue);
  }

  /** Nothing to remove is the state being asked for, so a missing row is not a failure. */
  private void remove(String key) {
    repository.findByKey(key).ifPresent(repository::delete);
  }

  private void write(String key, String value) {
    var row = repository.findByKey(key).orElseGet(() -> new ServerSettings(key, null));
    row.setValue(value);
    repository.save(row);
  }

  /**
   * A row that will not decrypt means the instance secret changed under stored credentials. That
   * is a licence this instance cannot use, and the fix is the same as never having had one — set
   * it again — so it is reported as such rather than handed on as a key that cannot sign.
   */
  private String decrypt(String stored) {
    try {
      return encryptor.decrypt(stored);
    } catch (EncryptionOperationNotPossibleException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED,
          "the stored licence credentials cannot be decrypted with this instance's secret; set"
              + " them again");
    }
  }
}
