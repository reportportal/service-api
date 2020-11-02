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

package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.GetBugTrackingSystemHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.integration.util.validator.IntegrationValidator;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter.TO_INTEGRATION_RESOURCE;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class GetIntegrationHandlerImpl implements GetIntegrationHandler {

	private final Map<String, IntegrationService> integrationServiceMapping;
	private final IntegrationService basicIntegrationService;
	private final IntegrationRepository integrationRepository;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final ProjectRepository projectRepository;
	private final GetBugTrackingSystemHandler getBugTrackingSystemHandler;

	@Autowired
	public GetIntegrationHandlerImpl(@Qualifier("integrationServiceMapping") Map<String, IntegrationService> integrationServiceMapping,
			@Qualifier("basicIntegrationServiceImpl") IntegrationService integrationService, IntegrationRepository integrationRepository,
			IntegrationTypeRepository integrationTypeRepository, ProjectRepository projectRepository,
			GetBugTrackingSystemHandler getBugTrackingSystemHandler) {
		this.integrationServiceMapping = integrationServiceMapping;
		this.basicIntegrationService = integrationService;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
		this.getBugTrackingSystemHandler = getBugTrackingSystemHandler;
	}

	@Override
	public IntegrationResource getProjectIntegrationById(Long integrationId, String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, project.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		return TO_INTEGRATION_RESOURCE.apply(integration);
	}

	@Override
	public IntegrationResource getGlobalIntegrationById(Long integrationId) {
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		return TO_INTEGRATION_RESOURCE.apply(integration);
	}

	@Override
	public Optional<Integration> getEnabledByProjectIdOrGlobalAndIntegrationGroup(Long projectId, IntegrationGroupEnum integrationGroup) {

		List<Long> integrationTypeIds = integrationTypeRepository.findAllByIntegrationGroup(integrationGroup)
				.stream()
				.map(IntegrationType::getId)
				.collect(Collectors.toList());

		List<Integration> integrations = integrationRepository.findAllByProjectIdAndInIntegrationTypeIds(projectId, integrationTypeIds);

		if (!CollectionUtils.isEmpty(integrations)) {

			return integrations.stream().filter(integration -> integration.getType().isEnabled() && integration.isEnabled()).findFirst();

		} else {

			return getGlobalIntegrationByIntegrationTypeIds(integrationTypeIds);
		}

	}

	@Override
	public Integration getEnabledBtsIntegration(ReportPortalUser.ProjectDetails projectDetails, String url, String btsProject) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = getBugTrackingSystemHandler.getEnabledProjectIntegration(projectDetails, url, btsProject)
				.orElseGet(() -> {
					Integration globalIntegration = getBugTrackingSystemHandler.getEnabledGlobalIntegration(url, btsProject)
							.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, url));

					IntegrationValidator.validateProjectLevelIntegrationConstraints(project, globalIntegration);

					return globalIntegration;
				});
		validateIntegration(integration);
		return integration;
	}

	@Override
	public Integration getEnabledBtsIntegration(ReportPortalUser.ProjectDetails projectDetails, Long integrationId) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = getBugTrackingSystemHandler.getEnabledProjectIntegration(projectDetails, integrationId).orElseGet(() -> {
			Integration globalIntegration = getBugTrackingSystemHandler.getEnabledGlobalIntegration(integrationId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

			IntegrationValidator.validateProjectLevelIntegrationConstraints(project, globalIntegration);

			return globalIntegration;
		});
		validateIntegration(integration);
		return integration;
	}

	@Override
	public Integration getEnabledBtsIntegration(Long integrationId) {

		Integration globalIntegration = getBugTrackingSystemHandler.getEnabledGlobalIntegration(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

		return globalIntegration;
	}

	@Override
	public List<IntegrationResource> getGlobalIntegrations() {
		return integrationRepository.findAllGlobal().stream().map(TO_INTEGRATION_RESOURCE).collect(Collectors.toList());
	}

	@Override
	public List<IntegrationResource> getGlobalIntegrations(String pluginName) {
		IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));
		return integrationRepository.findAllGlobalByType(integrationType)
				.stream()
				.map(TO_INTEGRATION_RESOURCE)
				.collect(Collectors.toList());
	}

	@Override
	public List<IntegrationResource> getProjectIntegrations(String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		return integrationRepository.findAllByProjectId(project.getId()).stream().map(TO_INTEGRATION_RESOURCE).collect(Collectors.toList());
	}

	@Override
	public List<IntegrationResource> getProjectIntegrations(String pluginName, String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));
		return integrationRepository.findAllByProjectIdAndType(project.getId(), integrationType)
				.stream()
				.map(TO_INTEGRATION_RESOURCE)
				.collect(Collectors.toList());
	}

	@Override
	public boolean testConnection(Long integrationId, String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, project.getId())
				.orElseGet(() -> integrationRepository.findGlobalById(integrationId)
						.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId)));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);
		return integrationService.checkConnection(integration);
	}

	@Override
	public boolean testConnection(Long integrationId) {
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

		IntegrationService integrationService = integrationServiceMapping.getOrDefault(integration.getType().getName(),
				this.basicIntegrationService
		);
		return integrationService.checkConnection(integration);
	}

	private Optional<Integration> getGlobalIntegrationByIntegrationTypeIds(List<Long> integrationTypeIds) {
		return integrationRepository.findAllGlobalInIntegrationTypeIds(integrationTypeIds)
				.stream()
				.filter(integration -> integration.getType().isEnabled() && integration.isEnabled())
				.findFirst();
	}

	private void validateIntegration(Integration integration) {
		BusinessRule.expect(integration, i -> integration.getType().isEnabled())
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("'{}' type integrations are disabled by Administrator", integration.getType().getName())
								.get()
				);
		BusinessRule.expect(integration, Integration::isEnabled)
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Integration with ID = '{}' is disabled", integration.getId()).get()
				);
	}
}
