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
import com.epam.ta.reportportal.core.integration.util.validator.IntegrationValidator;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class GetIntegrationHandlerImpl implements GetIntegrationHandler {

	private final IntegrationRepository integrationRepository;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final ProjectRepository projectRepository;
	private final GetBugTrackingSystemHandler getBugTrackingSystemHandler;

	@Autowired
	public GetIntegrationHandlerImpl(IntegrationRepository integrationRepository, IntegrationTypeRepository integrationTypeRepository,
			ProjectRepository projectRepository, GetBugTrackingSystemHandler getBugTrackingSystemHandler) {
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
		this.getBugTrackingSystemHandler = getBugTrackingSystemHandler;
	}

	@Override
	public IntegrationResource getProjectIntegrationById(Long integrationId, ReportPortalUser.ProjectDetails projectDetails) {

		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		return IntegrationConverter.TO_INTEGRATION_RESOURCE.apply(integration);

	}

	@Override
	public IntegrationResource getGlobalIntegrationById(Long integrationId) {
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

		return IntegrationConverter.TO_INTEGRATION_RESOURCE.apply(integration);
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

	private Optional<Integration> getGlobalIntegrationByIntegrationTypeIds(List<Long> integrationTypeIds) {
		return integrationRepository.findAllGlobalInIntegrationTypeIds(integrationTypeIds)
				.stream().filter(integration -> integration.getType().isEnabled() && integration.isEnabled())
				.findFirst();
	}

	private void validateIntegration(Integration integration) {
		BusinessRule.expect(integration, i -> integration.getType().isEnabled()).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("'{}' type integrations are disabled by Administrator", integration.getType().getName()).get()
		);
		BusinessRule.expect(integration, Integration::isEnabled).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Integration with ID = '{}' is disabled", integration.getId()).get()
		);
	}
}
