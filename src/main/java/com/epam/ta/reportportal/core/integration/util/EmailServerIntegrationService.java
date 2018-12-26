package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.admin.ServerAdminHandlerImpl;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.mchange.lang.IntegerUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class EmailServerIntegrationService implements IntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAdminHandlerImpl.class);

	private final IntegrationTypeRepository integrationTypeRepository;
	private final IntegrationRepository integrationRepository;
	private final BasicTextEncryptor basicTextEncryptor;
	private final MailServiceFactory emailServiceFactory;

	@Autowired
	public EmailServerIntegrationService(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			BasicTextEncryptor basicTextEncryptor, MailServiceFactory emailServiceFactory) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.integrationRepository = integrationRepository;
		this.basicTextEncryptor = basicTextEncryptor;
		this.emailServiceFactory = emailServiceFactory;
	}

	@Override
	public Integration createGlobalIntegration(String integrationName, Map<String, Object> integrationParams) {

		updateIntegrationParams(integrationParams);

		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationName,
				IntegrationGroupEnum.NOTIFICATION
		).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Email server integration with name - '{}' not found.", integrationName).get()
		));

		Integration integration = retrieveIntegration(integrationRepository.getAllGlobalIntegrationsByType(integrationType));

		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);

		testConnection(integration);

		return integration;

	}

	@Override
	public Integration createProjectIntegration(String integrationName, ReportPortalUser.ProjectDetails projectDetails,
			Map<String, Object> integrationParams) {

		updateIntegrationParams(integrationParams);

		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationName,
				IntegrationGroupEnum.NOTIFICATION
		).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Email server integration with name - '{}' not found.", integrationName).get()
		));

		Integration integration = retrieveIntegration(integrationRepository.findAllByProjectIdAndType(projectDetails.getProjectId(),
				integrationType
		));

		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);

		testConnection(integration);

		return integration;
	}

	private void updateIntegrationParams(Map<String, Object> integrationParams) {

		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		int port = ofNullable(integrationParams.get(ServerSettingsEnum.PORT.getAttribute())).map(p -> IntegerUtils.parseInt(String.valueOf(p),
				25
		)).orElse(25);

		if ((port <= 0) || (port > 65535)) {
			BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
		}

		integrationParams.put(ServerSettingsEnum.PORT.getAttribute(), port);

		if (!ServerSettingsEnum.PROTOCOL.getAttribute(integrationParams).isPresent()) {
			integrationParams.put(ServerSettingsEnum.PROTOCOL.getAttribute(), "smtp");
		}

		if (ofNullable(integrationParams.get(ServerSettingsEnum.AUTH_ENABLED.getAttribute())).map(e -> BooleanUtils.toBoolean(String.valueOf(
				e))).orElse(false)) {
			ServerSettingsEnum.PASSWORD.getAttribute(integrationParams)
					.ifPresent(password -> integrationParams.put(ServerSettingsEnum.PASSWORD.getAttribute(),
							basicTextEncryptor.encrypt(password)
					));
		} else {
			/* Auto-drop values on switched-off authentication */
			integrationParams.put(ServerSettingsEnum.USERNAME.getAttribute(), null);
			integrationParams.put(ServerSettingsEnum.PASSWORD.getAttribute(), null);
		}
	}

	private Integration retrieveIntegration(List<Integration> integrations) {
		Integration integration = integrations.stream().findFirst().orElseGet(() -> {
			Integration newIntegration = new Integration();
			newIntegration.setCreationDate(LocalDateTime.now());
			return newIntegration;
		});

		integrations.removeIf(i -> !i.getId().equals(integration.getId()));

		return integration;
	}

	private void testConnection(Integration integration) {
		Optional<EmailService> emailService = emailServiceFactory.getEmailService(integration);
		if (emailService.isPresent()) {
			try {
				emailService.get().testConnection();
			} catch (MessagingException ex) {
				LOGGER.error("Cannot send email to user", ex);
				fail().withError(FORBIDDEN_OPERATION,
						"Email configuration is incorrect. Please, check your configuration. " + ex.getMessage()
				);
			}
		}
	}

}

