package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.entity.integration.Integration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface IntegrationService {

	Integration createIntegration(String integrationName, Map<String, Object> integrationParams);
}
