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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.GetBugTrackingSystemHandler;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetBugTrackingSystemHandlerImpl implements GetBugTrackingSystemHandler {

	private final ProjectRepository projectRepository;
	private final IntegrationRepository integrationRepository;

	@Autowired
	public GetBugTrackingSystemHandlerImpl(ProjectRepository projectRepository, IntegrationRepository integrationRepository) {
		this.projectRepository = projectRepository;
		this.integrationRepository = integrationRepository;
	}

	@Override
	public Integration getEnabledProjectOrGlobalIntegrationByUrlAndBtsProject(ReportPortalUser.ProjectDetails projectDetails, String url,
			String btsProject) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = integrationRepository.findProjectBtsByUrlAndLinkedProject(url, btsProject, project.getId())
				.orElseGet(() -> {

					Integration globalIntegration = integrationRepository.findGlobalBtsByUrlAndLinkedProject(url, btsProject)
							.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, url));

					validateProjectLevelConstraints(project, globalIntegration);

					return globalIntegration;

				});

		validateBtsIntegration(integration);

		return integration;
	}

	@Override
	public Integration getEnabledByProjectIdAndIdOrGlobalById(ReportPortalUser.ProjectDetails projectDetails,
			Long integrationId) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectName()));

		Integration integration = integrationRepository.findByIdAndProjectId(integrationId, project.getId()).orElseGet(() -> {

			Integration globalIntegration = integrationRepository.findGlobalById(integrationId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

			validateProjectLevelConstraints(project, globalIntegration);

			return globalIntegration;

		});

		validateBtsIntegration(integration);

		return integration;
	}

	private void validateProjectLevelConstraints(Project project, Integration integration) {

		BusinessRule.expect(project.getIntegrations()
				.stream()
				.map(Integration::getType)
				.noneMatch(it -> it.getIntegrationGroup() == integration.getType().getIntegrationGroup()), equalTo(true))
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
						"Global integration with ID = '{}' has been found, but you cannot use it, because you have project-level integration(s) of that type",
						integration.getId()
				).get());
	}

	private void validateBtsIntegration(Integration integration) {

		BusinessRule.expect(integration, it -> IntegrationGroupEnum.BTS == it.getType().getIntegrationGroup())
				.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
						"Unable to test connection to the integration with type - '{}', Allowed type(es): '{}'",
						integration.getType().getIntegrationGroup(),
						IntegrationGroupEnum.BTS
				));

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
