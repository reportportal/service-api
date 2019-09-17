package com.epam.ta.reportportal.core.integration.plugin.util;

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */

public class IntegrationTypePropertiesExtractor {

	private IntegrationTypePropertiesExtractor() {
		//static only
	}

	public static Optional<String> extractProperty(IntegrationType integrationType, IntegrationTypeProperties properties) {
		Map<String, Object> details = ofNullable(integrationType.getDetails()).flatMap(integrationTypeDetails -> ofNullable(
				integrationTypeDetails.getDetails()))
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION));

		return properties.getValue(details).map(String::valueOf);
	}
}
