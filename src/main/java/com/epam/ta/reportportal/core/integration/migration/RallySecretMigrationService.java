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
public class RallySecretMigrationService extends AbstractSecretMigrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RallySecretMigrationService.class);
	private static final String RALLY_INTEGRATION_TYPE_NAME = "rally";

	@Autowired
	public RallySecretMigrationService(IntegrationRepository integrationRepository, BasicTextEncryptor encryptor) {
		super(integrationRepository, encryptor);
	}

	@Transactional
	public void migrate() {
		LOGGER.debug("Migration of rally secrets has been started");
		integrationRepository.findAllByTypeIn(RALLY_INTEGRATION_TYPE_NAME).forEach(it -> extractParams(it).ifPresent(params -> {
			BtsProperties.OAUTH_ACCESS_KEY.getParam(params)
					.ifPresent(key -> BtsProperties.OAUTH_ACCESS_KEY.setParam(it.getParams(), encryptor.encrypt(key)));
			BtsProperties.PASSWORD.getParam(params)
					.ifPresent(pass -> BtsProperties.PASSWORD.setParam(it.getParams(), encryptor.encrypt(pass)));
		}));
		LOGGER.debug("Migration of rally secrets has been finished");
	}

}
