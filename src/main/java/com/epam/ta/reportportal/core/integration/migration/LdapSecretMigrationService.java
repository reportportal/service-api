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

import com.epam.ta.reportportal.dao.IntegrationRepository;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class LdapSecretMigrationService extends AbstractSecretMigrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdapSecretMigrationService.class);
	private static final String LDAP_INTEGRATION_TYPE_NAME = "ldap";
	private static final String LDAP_SECURE_PROPERTY = "managerPassword";

	@Value("${rp.auth.encryptor.password:reportportal}")
	private String salt;

	public LdapSecretMigrationService(IntegrationRepository integrationRepository, BasicTextEncryptor encryptor) {
		super(integrationRepository, encryptor);
	}

	@Transactional
	public void migrate() {
		LOGGER.debug("Migration of LDAP secrets has been started");

		BasicTextEncryptor propertySaltEncryptor = new BasicTextEncryptor();
		propertySaltEncryptor.setPassword(salt);

		integrationRepository.findAllByTypeIn(LDAP_INTEGRATION_TYPE_NAME)
				.forEach(it -> extractParams(it).ifPresent(params -> ofNullable(params.get(LDAP_SECURE_PROPERTY)).map(param -> (String) param)
						.ifPresent(param -> params.put(LDAP_SECURE_PROPERTY, encryptor.encrypt(propertySaltEncryptor.decrypt(param))))));

		LOGGER.debug("Migration of LDAP secrets has been finished");
	}
}
