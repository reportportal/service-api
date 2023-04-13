/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.integration.migration.JiraEmailSecretMigrationService;
import com.epam.ta.reportportal.core.integration.migration.LdapSecretMigrationService;
import com.epam.ta.reportportal.core.integration.migration.RallySecretMigrationService;
import com.epam.ta.reportportal.core.integration.migration.SaucelabsSecretMigrationService;
import com.epam.ta.reportportal.entity.enums.FeatureFlag;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.util.FeatureFlagHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
@Profile("!unittest")
public class IntegrationSecretsMigrationHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(IntegrationSecretsMigrationHandler.class);

  private static final String SECRETS_PATH = "integration-secrets";
  @Value("${rp.integration.salt.path:keystore}")
  private String integrationSaltPath;

  @Value("${rp.integration.salt.migration:migration}")
  private String migrationFile;

  private final DataStore dataStore;

  private final JiraEmailSecretMigrationService jiraEmailSecretMigrationService;

  private final RallySecretMigrationService rallySecretMigrationService;

  private final SaucelabsSecretMigrationService saucelabsSecretMigrationService;

  private final LdapSecretMigrationService ldapSecretMigrationService;

  private final FeatureFlagHandler featureFlagHandler;

  /**
   * Creates instance of IntegrationSecretsMigrationHandler.
   *
   * @param dataStore                       {@link DataStore}
   * @param jiraEmailSecretMigrationService {@link JiraEmailSecretMigrationService}
   * @param rallySecretMigrationService     {@link RallySecretMigrationService}
   * @param saucelabsSecretMigrationService {@link SaucelabsSecretMigrationService}
   * @param ldapSecretMigrationService      {@link LdapSecretMigrationService}
   * @param featureFlagHandler              {@link FeatureFlagHandler}
   */
  @Autowired
  public IntegrationSecretsMigrationHandler(DataStore dataStore,
      JiraEmailSecretMigrationService jiraEmailSecretMigrationService,
      RallySecretMigrationService rallySecretMigrationService,
      SaucelabsSecretMigrationService saucelabsSecretMigrationService,
      LdapSecretMigrationService ldapSecretMigrationService,
      FeatureFlagHandler featureFlagHandler) {
    this.dataStore = dataStore;
    this.jiraEmailSecretMigrationService = jiraEmailSecretMigrationService;
    this.rallySecretMigrationService = rallySecretMigrationService;
    this.saucelabsSecretMigrationService = saucelabsSecretMigrationService;
    this.ldapSecretMigrationService = ldapSecretMigrationService;
    this.featureFlagHandler = featureFlagHandler;
  }

  /**
   * Migration of user info from Auth Provider.
   *
   * @param event {@link ApplicationReadyEvent}
   */
  @EventListener
  public void migrate(ApplicationReadyEvent event) throws IOException {
    String migrationFilePath;
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      migrationFilePath = Paths.get(SECRETS_PATH, migrationFile).toString();
    } else {
      migrationFilePath = integrationSaltPath + File.separator + migrationFile;
    }
    try (InputStream load = dataStore.load(migrationFilePath)) {
      jiraEmailSecretMigrationService.migrate();
      rallySecretMigrationService.migrate();
      saucelabsSecretMigrationService.migrate();
      ldapSecretMigrationService.migrate();
      dataStore.delete(migrationFilePath);
    } catch (ReportPortalException ex) {
      LOGGER.info("Secrets migration is not needed");
    }
  }
}
