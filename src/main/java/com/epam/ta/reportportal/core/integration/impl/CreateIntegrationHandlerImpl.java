package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateIntegrationHandlerImpl implements CreateIntegrationHandler {

	private final Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping;

	private final IntegrationRepository integrationRepository;

	private final IntegrationTypeRepository integrationTypeRepository;

	private final ProjectRepository projectRepository;

	@Autowired
	public CreateIntegrationHandlerImpl(Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping,
			IntegrationRepository integrationRepository, IntegrationTypeRepository integrationTypeRepository,
			ProjectRepository projectRepository) {
		this.integrationServiceMapping = integrationServiceMapping;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS createGlobalIntegration(UpdateIntegrationRQ updateRequest) {

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		Integration integration = integrationServiceMapping.get(reportPortalIntegration)
				.createGlobalIntegration(updateRequest.getIntegrationName(), updateRequest.getIntegrationParams());

		integration.setEnabled(updateRequest.getEnabled());

		integrationRepository.save(integration);

		IntegrationType integrationType = integrationTypeRepository.findByIntegrationGroup(integration.getType().getIntegrationGroup())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND,
						"Unknown integration group - " + integration.getType().getIntegrationGroup()
				));

		integrationRepository.updateIntegrationGroupEnabledState(updateRequest.getEnabled(), integrationType.getId());

		return new OperationCompletionRS("Email integration with id = " + integration.getId() + " has been successfully created.");

	}

	@Override
	public OperationCompletionRS createProjectIntegration(ReportPortalUser.ProjectDetails projectDetails,
			UpdateIntegrationRQ updateRequest) {

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		Integration integration = integrationServiceMapping.get(reportPortalIntegration)
				.createProjectIntegration(updateRequest.getIntegrationName(), projectDetails, updateRequest.getIntegrationParams());

		integration.setEnabled(updateRequest.getEnabled());

		integration.setProject(project);

		integrationRepository.save(integration);

		return new OperationCompletionRS("Email integration with id = " + integration.getId() + " has been successfully created.");
	}

}
