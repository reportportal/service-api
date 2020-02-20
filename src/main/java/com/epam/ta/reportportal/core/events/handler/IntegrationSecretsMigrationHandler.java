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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class IntegrationSecretsMigrationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationSecretsMigrationHandler.class);

	@Value("${rp.integration.salt.path:keystore}")
	private String integrationSaltPath;

	@Value("${rp.integration.salt.migration:migration}")
	private String migrationFile;

	private final DataStore dataStore;

	private final JiraEmailSecretMigrationService jiraEmailSecretMigrationService;

	private final RallySecretMigrationService rallySecretMigrationService;

	private final SaucelabsSecretMigrationService saucelabsSecretMigrationService;

	private final LdapSecretMigrationService ldapSecretMigrationService;

	@Autowired
	public IntegrationSecretsMigrationHandler(DataStore dataStore, JiraEmailSecretMigrationService jiraEmailSecretMigrationService,
			RallySecretMigrationService rallySecretMigrationService, SaucelabsSecretMigrationService saucelabsSecretMigrationService,
			LdapSecretMigrationService ldapSecretMigrationService) {
		this.dataStore = dataStore;
		this.jiraEmailSecretMigrationService = jiraEmailSecretMigrationService;
		this.rallySecretMigrationService = rallySecretMigrationService;
		this.saucelabsSecretMigrationService = saucelabsSecretMigrationService;
		this.ldapSecretMigrationService = ldapSecretMigrationService;
	}

	@EventListener
	public void migrate(ApplicationReadyEvent event) {
		try {
			final String migrationFilePath = integrationSaltPath + File.separator + migrationFile;
			dataStore.load(migrationFilePath);

			ExecutorService executor = Executors.newFixedThreadPool(4);
			executor.execute(jiraEmailSecretMigrationService::migrate);
			executor.execute(rallySecretMigrationService::migrate);
			executor.execute(saucelabsSecretMigrationService::migrate);
			executor.execute(ldapSecretMigrationService::migrate);
			executor.shutdown();

			dataStore.delete(migrationFilePath);
		} catch (ReportPortalException ex) {
			LOGGER.info("Secrets migration is not needed");
		}
	}
}
