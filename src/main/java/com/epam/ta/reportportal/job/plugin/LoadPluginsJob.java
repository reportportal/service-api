/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.job.plugin;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.configs.Conditions;
import com.epam.ta.reportportal.core.integration.plugin.loader.PluginLoader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.epam.reportportal.extension.common.IntegrationTypeProperties.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Conditional(Conditions.NotTestCondition.class)
@Service
public class LoadPluginsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoadPluginsJob.class);

	private final IntegrationTypeRepository integrationTypeRepository;
	private final PluginLoader pluginLoader;

	@Autowired
	public LoadPluginsJob(IntegrationTypeRepository integrationTypeRepository, PluginLoader pluginLoader) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginLoader = pluginLoader;
	}

	@Scheduled(fixedDelayString = "${com.ta.reportportal.job.load.plugins.cron}")
	public void execute() {
		integrationTypeRepository.findAll()
				.stream()
				.filter(IntegrationType::isEnabled)
				.filter(it -> Objects.nonNull(it.getDetails()) && Objects.nonNull(it.getDetails().getDetails()))
				.filter(this::isMandatoryFieldsExist)
				.forEach(it -> {
					final boolean loaded = pluginLoader.load(it);
					if (loaded) {
						LOGGER.debug(Suppliers.formattedSupplier("Plugin - '{}' has been successfully started.", it.getName()).get());
					} else {
						LOGGER.error(Suppliers.formattedSupplier("Plugin - '{}' has not been started.", it.getName()).get());
					}
				});
	}

	private boolean isMandatoryFieldsExist(IntegrationType integrationType) {
		Map<String, Object> details = integrationType.getDetails().getDetails();
		return Stream.of(FILE_ID, VERSION, FILE_NAME).allMatch(property -> property.getValue(details).isPresent());

	}

}
