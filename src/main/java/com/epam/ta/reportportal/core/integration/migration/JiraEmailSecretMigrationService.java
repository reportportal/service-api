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

package com.epam.ta.reportportal.core.integration.migration;

import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class JiraEmailSecretMigrationService extends AbstractSecretMigrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraEmailSecretMigrationService.class);
	private static final String JIRA_INTEGRATION_TYPE_NAME = "jira";
	private static final String EMAIL_INTEGRATION_TYPE_NAME = "email";

	@Autowired
	public JiraEmailSecretMigrationService(IntegrationRepository integrationRepository, BasicTextEncryptor encryptor) {
		super(integrationRepository, encryptor);
	}

	@Transactional
	public void migrate() {
		LOGGER.debug("Migration of jira and email secrets has been started");

		BasicTextEncryptor staticSaltEncryptor = new BasicTextEncryptor();
		staticSaltEncryptor.setPassword("reportportal");

		integrationRepository.findAllByTypeIn(JIRA_INTEGRATION_TYPE_NAME, EMAIL_INTEGRATION_TYPE_NAME)
				.forEach(it -> extractParams(it).flatMap(BtsProperties.PASSWORD::getParam)
						.ifPresent(pass -> BtsProperties.PASSWORD.setParam(it.getParams(),
								encryptor.encrypt(staticSaltEncryptor.decrypt(pass))
						)));

		LOGGER.debug("Migration of jira and email secrets has been finished");
	}
}
