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

import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.integration.util.property.SauceLabsProperties;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

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

	private final BasicTextEncryptor encryptor;

	private final IntegrationRepository integrationRepository;

	@Autowired
	public IntegrationSecretsMigrationHandler(DataStore dataStore, BasicTextEncryptor encryptor,
			IntegrationRepository integrationRepository) {
		this.dataStore = dataStore;
		this.encryptor = encryptor;
		this.integrationRepository = integrationRepository;
	}

	@EventListener
	@Transactional
	public void migrate(ApplicationReadyEvent event) {
		try {
			final String migrationFilePath = integrationSaltPath + File.separator + migrationFile;
			dataStore.load(migrationFilePath);

			BasicTextEncryptor staticSaltEncryptor = new BasicTextEncryptor();
			staticSaltEncryptor.setPassword("reportportal");

			final Map<String, List<Integration>> groupedByType = integrationRepository.findAllPredefined()
					.stream()
					.collect(Collectors.groupingBy(it -> it.getType().getName()));

			ofNullable(groupedByType.get("jira")).ifPresent(collection -> {
				collection.forEach(passwordUpdatingConsumer(staticSaltEncryptor));
			});

			ofNullable(groupedByType.get("email")).ifPresent(collection -> {
				collection.forEach(passwordUpdatingConsumer(staticSaltEncryptor));
			});

			ofNullable(groupedByType.get("rally")).ifPresent(collection -> collection.forEach(it -> {
				BtsProperties.OAUTH_ACCESS_KEY.getParam(it.getParams().getParams())
						.ifPresent(key -> BtsProperties.OAUTH_ACCESS_KEY.setParam(it.getParams(), encryptor.encrypt(key)));
				BtsProperties.PASSWORD.getParam(it.getParams().getParams())
						.ifPresent(pass -> BtsProperties.PASSWORD.setParam(it.getParams(), encryptor.encrypt(pass)));
			}));

			ofNullable(groupedByType.get("saucelabs")).ifPresent(collection -> collection.forEach(it -> SauceLabsProperties.ACCESS_TOKEN.getParameter(
					it.getParams().getParams())
					.ifPresent(key -> SauceLabsProperties.ACCESS_TOKEN.setParameter(it.getParams(), encryptor.encrypt(key)))));
			dataStore.delete(migrationFilePath);
		} catch (ReportPortalException ex) {
			LOGGER.info("Secrets migration is not needed");
		}
	}

	private Consumer<Integration> passwordUpdatingConsumer(BasicTextEncryptor staticSaltEncryptor) {
		return it -> BtsProperties.PASSWORD.getParam(it.getParams().getParams())
				.ifPresent(pass -> BtsProperties.PASSWORD.setParam(it.getParams(), encryptor.encrypt(staticSaltEncryptor.decrypt(pass))));
	}

}
