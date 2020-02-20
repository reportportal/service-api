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
import com.epam.ta.reportportal.entity.integration.Integration;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AbstractSecretMigrationService {

	protected IntegrationRepository integrationRepository;

	protected BasicTextEncryptor encryptor;

	public AbstractSecretMigrationService(IntegrationRepository integrationRepository, BasicTextEncryptor encryptor) {
		this.integrationRepository = integrationRepository;
		this.encryptor = encryptor;
	}

	abstract public void migrate();

	protected static Optional<Map<String, Object>> extractParams(Integration integration) {
		return ofNullable(integration.getParams()).map(it -> ofNullable(it.getParams()).orElse(Collections.emptyMap()));
	}
}
