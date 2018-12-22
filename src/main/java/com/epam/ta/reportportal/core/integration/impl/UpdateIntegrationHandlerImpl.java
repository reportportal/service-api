package com.epam.ta.reportportal.core.integration.impl;

import com.epam.ta.reportportal.core.integration.UpdateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.util.IntegrationService;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
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
public class UpdateIntegrationHandlerImpl implements UpdateIntegrationHandler {

	private final Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping;

	private final IntegrationRepository integrationRepository;

	@Autowired
	public UpdateIntegrationHandlerImpl(Map<ReportPortalIntegrationEnum, IntegrationService> integrationServiceMapping,
			IntegrationRepository integrationRepository) {
		this.integrationServiceMapping = integrationServiceMapping;
		this.integrationRepository = integrationRepository;
	}

	@Override
	public OperationCompletionRS updateIntegration(UpdateIntegrationRQ updateRequest) {

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(updateRequest.getIntegrationName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, updateRequest.getIntegrationName()));

		Integration integration = integrationServiceMapping.get(reportPortalIntegration)
				.createIntegration(updateRequest.getIntegrationName(), updateRequest.getIntegrationParams());

		integration.setEnabled(updateRequest.getEnabled());

		integrationRepository.save(integration);

		return new OperationCompletionRS("Email integration with id = " + integration.getId() + " has been successfully created.");

	}

}
