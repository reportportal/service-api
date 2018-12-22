package com.epam.ta.reportportal.core.integration;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface UpdateIntegrationHandler {

	OperationCompletionRS updateIntegration(UpdateIntegrationRQ updateRequest);
}
