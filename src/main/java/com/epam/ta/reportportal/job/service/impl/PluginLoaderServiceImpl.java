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

package com.epam.ta.reportportal.job.service.impl;

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.ReservedIntegrationTypeEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.job.service.PluginLoaderService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.epam.reportportal.extension.common.IntegrationTypeProperties.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderServiceImpl implements PluginLoaderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoaderServiceImpl.class);

	private final IntegrationTypeRepository integrationTypeRepository;
	private final Pf4jPluginBox pluginBox;

	@Autowired
	public PluginLoaderServiceImpl(IntegrationTypeRepository integrationTypeRepository, Pf4jPluginBox pluginBox) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public List<PluginInfo> getNotLoadedPluginsInfo() {

		LOGGER.debug("Searching for not loaded plugins...");

		List<PluginInfo> notLoadedPlugins = Lists.newArrayList();

		integrationTypeRepository.findAll()
				.stream()
				.filter(IntegrationType::isEnabled)
				.filter(it -> it.getDetails() != null && it.getDetails().getDetails() != null)
				.filter(this::isMandatoryFieldsExist)
				.forEach(it -> {

					Map<IntegrationTypeProperties, Object> pluginProperties = retrievePluginProperties(it);

					Optional<PluginWrapper> pluginWrapper = pluginBox.getPluginById(it.getName());
					if (pluginWrapper.isEmpty() || !String.valueOf(pluginProperties.get(VERSION))
							.equalsIgnoreCase(pluginWrapper.get().getDescriptor().getVersion())) {

						PluginInfo pluginInfo = new PluginInfo(it.getName(),
								String.valueOf(pluginProperties.get(VERSION)),
								String.valueOf(pluginProperties.get(FILE_ID)),
								String.valueOf(pluginProperties.get(FILE_NAME)),
								it.isEnabled()
						);

						notLoadedPlugins.add(pluginInfo);
					}

				});

		LOGGER.debug(Suppliers.formattedSupplier("{} not loaded plugins have been found", notLoadedPlugins.size()).get());

		return notLoadedPlugins;
	}

	@Transactional
	@Override
	public void checkAndDeleteIntegrationType(IntegrationType integrationType) {
		if (isIntegrationTypeAvailableForRemoving(integrationType)) {
			integrationTypeRepository.deleteById(integrationType.getId());
		}
	}

	private boolean isMandatoryFieldsExist(IntegrationType integrationType) {
		Map<String, Object> details = integrationType.getDetails().getDetails();
		return Stream.of(FILE_ID, VERSION, FILE_NAME).allMatch(property -> property.getValue(details).isPresent());

	}

	private Map<IntegrationTypeProperties, Object> retrievePluginProperties(IntegrationType integrationType) {

		Map<String, Object> details = integrationType.getDetails().getDetails();
		Map<IntegrationTypeProperties, Object> pluginProperties = Maps.newHashMapWithExpectedSize(IntegrationTypeProperties.values().length);
		Arrays.stream(IntegrationTypeProperties.values())
				.forEach(property -> property.getValue(details).ifPresent(value -> pluginProperties.put(property, value)));
		return pluginProperties;
	}

	private boolean isIntegrationTypeAvailableForRemoving(IntegrationType integrationType) {
		/* hack: while email, ad, ldap, saml aren't  plugins - it shouldn't be proceeded as a plugin */
		if (ReservedIntegrationTypeEnum.fromName(integrationType.getName()).isPresent()) {
			return false;
		} else {
			return pluginBox.getPluginById(integrationType.getName()).map(p -> {
				if (pluginBox.unloadPlugin(integrationType)) {
					try {
						Files.deleteIfExists(p.getPluginPath());
						return true;
					} catch (IOException ex) {
						LOGGER.error("Error has occurred during plugin removing from the root directory", ex);
						return false;
					}
				} else {
					return false;
				}
			}).orElse(true);
		}
	}
}
