package com.epam.ta.reportportal.core.integration.util.property;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum ReportPortalIntegrationEnum {

	JIRA,
	RALLY,
	EMAIL;

	public static Optional<ReportPortalIntegrationEnum> findByName(String name) {
		return Arrays.stream(ReportPortalIntegrationEnum.values())
				.filter(integration -> integration.name().equalsIgnoreCase(name))
				.findFirst();
	}
}
