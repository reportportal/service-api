package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.util.property.JiraProperties;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class JiraIntegrationService implements IntegrationService {

	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public JiraIntegrationService(IntegrationTypeRepository integrationTypeRepository) {
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public Integration createIntegration(String integrationName, Map<String, Object> integrationParams) {

		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationName, IntegrationGroupEnum.BTS)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("BTS integration with name - '{}' not found.", integrationName).get()
				));

		AuthType authType = AuthType.findByName(JiraProperties.AUTH_TYPE.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"No auth property provided for Jira integration"
				)));

		if (AuthType.BASIC.equals(authType)) {
			expect(JiraProperties.USER_NAME.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Username value cannot be NULL"
			);
			expect(JiraProperties.PASSWORD.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Password value cannot be NULL"
			);
		} else if (AuthType.OAUTH.equals(authType)) {
			expect(JiraProperties.OAUTH_ACCESS_KEY.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"AccessKey value cannot be NULL"
			);
		} else {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					"Unsupported auth type for Jira integration - " + authType.name()
			);
		}
		expect(JiraProperties.PROJECT.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA project value cannot be NULL"
		);
		expect(JiraProperties.URL.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA URL value cannot be NULL"
		);

		Integration integration = new Integration();
		integration.setCreationDate(LocalDateTime.now());
		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);

		return integration;
	}
}
