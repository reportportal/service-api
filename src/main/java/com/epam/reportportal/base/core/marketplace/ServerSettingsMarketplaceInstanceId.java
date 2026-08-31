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

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps the instance id in {@code server_settings}, beside the licence credentials — the same
 * table, the same instance-scoped lifetime, and no migration, since it is a key/value row.
 *
 * <p>Generated on first read and never regenerated: a value that changed would make one instance
 * look like many, which is the one thing this identifier exists to prevent.
 */
@Component
public class ServerSettingsMarketplaceInstanceId implements MarketplaceInstanceId {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ServerSettingsMarketplaceInstanceId.class);

  static final String INSTANCE_ID_KEY = "marketplace.instance.id";

  private final ServerSettingsRepository repository;
  private final boolean enabled;

  /**
   * Creates the store.
   *
   * @param repository server settings
   * @param enabled    whether this instance reports a stable id at all
   */
  public ServerSettingsMarketplaceInstanceId(ServerSettingsRepository repository,
      @Value("${marketplace.analytics.instance-id.enabled:true}") boolean enabled) {
    this.repository = repository;
    this.enabled = enabled;
  }

  /**
   * The stored id, generating one on first use.
   *
   * <p>Its own transaction: this is called from the install path, and a failure to write an
   * analytics row must not roll back an install that otherwise succeeded. For the same reason
   * every failure below is swallowed after a warning — losing one instance's analytics is not a
   * reason to refuse a plugin someone asked for.
   *
   * @return the id, or empty when disabled or unwritable
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Optional<String> current() {
    if (!enabled) {
      return Optional.empty();
    }
    try {
      var existing = repository.findByKey(INSTANCE_ID_KEY).map(ServerSettings::getValue);
      if (existing.isPresent()) {
        return existing;
      }
      var generated = UUID.randomUUID().toString();
      repository.save(new ServerSettings(INSTANCE_ID_KEY, generated));
      return Optional.of(generated);
    } catch (DataIntegrityViolationException e) {
      // Two nodes of one instance generating at once: whoever lost re-reads the winner's value,
      // because the point is that the instance has ONE id, not that this node wrote it.
      return repository.findByKey(INSTANCE_ID_KEY).map(ServerSettings::getValue);
    } catch (RuntimeException e) {
      LOGGER.warn("Could not read or create the marketplace instance id: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
