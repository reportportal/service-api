/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
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

	@Transactional
	@Override
	public List<PluginInfo> getAllPluginsInfo(List<IntegrationType> integrationTypes) {
		LOGGER.info("Searching for all plugins...");

		List<PluginInfo> pluginInfoList = Lists.newArrayList();

		integrationTypes.stream()
				.filter(it -> it.getDetails() != null && it.getDetails().getDetails() != null)
				.filter(this::isMandatoryFieldsExist)
				.forEach(it -> {

					Map<IntegrationDetailsProperties, String> pluginProperties = retrievePluginProperties(it);

					PluginInfo pluginInfo = new PluginInfo(
							pluginProperties.get(IntegrationDetailsProperties.FILE_ID),
							pluginProperties.get(IntegrationDetailsProperties.VERSION),
							pluginProperties.get(IntegrationDetailsProperties.FILE_NAME)
					);

					pluginInfoList.add(pluginInfo);

				});

		LOGGER.info(Suppliers.formattedSupplier("{} plugins have been found", pluginInfoList.size()).get());

		return pluginInfoList;
	}

	@Transactional
	@Override
	public List<PluginInfo> getNotLoadedPluginsInfo(List<IntegrationType> integrationTypes) {

		LOGGER.info("Searching for not loaded plugins...");

		List<PluginInfo> notLoadedPlugins = Lists.newArrayList();

		integrationTypes.stream()
				.filter(it -> it.getDetails() != null && it.getDetails().getDetails() != null)
				.filter(this::isMandatoryFieldsExist)
				.forEach(it -> {

					Map<IntegrationDetailsProperties, String> pluginProperties = retrievePluginProperties(it);

					Optional<PluginWrapper> pluginWrapper = pluginBox.getPluginById(it.getName());

					if (!pluginWrapper.isPresent() || !pluginProperties.get(IntegrationDetailsProperties.VERSION)
							.equalsIgnoreCase(pluginWrapper.get().getDescriptor().getVersion())) {

						PluginInfo pluginInfo = new PluginInfo(
								pluginProperties.get(IntegrationDetailsProperties.FILE_ID),
								pluginProperties.get(IntegrationDetailsProperties.VERSION),
								pluginProperties.get(IntegrationDetailsProperties.FILE_NAME)
						);

						notLoadedPlugins.add(pluginInfo);
					}

				});

		LOGGER.info(Suppliers.formattedSupplier("{} not loaded plugins have been found", notLoadedPlugins.size()).get());

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
		return Arrays.stream(IntegrationDetailsProperties.values()).allMatch(property -> property.getValue(details).isPresent());

	}

	private Map<IntegrationDetailsProperties, String> retrievePluginProperties(IntegrationType integrationType) {

		Map<String, Object> details = integrationType.getDetails().getDetails();
		Map<IntegrationDetailsProperties, String> pluginProperties = Maps.newHashMapWithExpectedSize(IntegrationDetailsProperties.values().length);
		Arrays.stream(IntegrationDetailsProperties.values())
				.forEach(property -> property.getValue(details).ifPresent(value -> pluginProperties.put(property, value)));

		return pluginProperties;
	}

	private boolean isIntegrationTypeAvailableForRemoving(IntegrationType integrationType) {

		return ReportPortalIntegrationEnum.findByName(integrationType.getName()).map(integration -> {
			if (integration.isPlugin()) {
				return pluginBox.getPluginById(integrationType.getName()).map(p -> {

					if (pluginBox.unloadPlugin(p.getPluginId())) {
						try {
							Files.deleteIfExists(p.getPluginPath());
							return true;
						} catch (IOException ex) {
							LOGGER.debug("Error has occurred during plugin removing from the root directory", ex);
							return false;
						}
					} else {
						return false;
					}

				}).orElse(true);

			} else {
				return false;
			}
		}).orElse(true);

	}
}
