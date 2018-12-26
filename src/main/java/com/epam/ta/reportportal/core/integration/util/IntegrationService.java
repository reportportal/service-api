package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.entity.integration.Integration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface IntegrationService {

	Integration createGlobalIntegration(String integrationName, Map<String, Object> integrationParams);

	Integration createProjectIntegration(String integrationName, ReportPortalUser.ProjectDetails projectDetails, Map<String, Object> integrationParams);
}
