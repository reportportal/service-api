/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.util;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.fail;
import static com.epam.reportportal.rules.exception.ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.core.admin.ServerAdminHandlerImpl;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.model.integration.IntegrationRQ;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Maps;
import com.mchange.lang.IntegerUtils;
import java.util.Map;
import java.util.Optional;
import jakarta.mail.MessagingException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for integration with email server.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class EmailServerIntegrationService extends BasicIntegrationServiceImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerAdminHandlerImpl.class);

  private final BasicTextEncryptor basicTextEncryptor;

  private final MailServiceFactory emailServiceFactory;

  /**
   * Constructs an EmailServerIntegrationService with the specified dependencies.
   *
   * @param integrationRepository the repository for integration entities
   * @param pluginBox             the plugin box for managing plugins
   * @param basicTextEncryptor    the text encryptor for encrypting sensitive data
   * @param emailServiceFactory   the factory for creating email services
   */
  public EmailServerIntegrationService(
      IntegrationRepository integrationRepository,
      PluginBox pluginBox,
      BasicTextEncryptor basicTextEncryptor,
      MailServiceFactory emailServiceFactory) {
    super(integrationRepository, pluginBox);
    this.basicTextEncryptor = basicTextEncryptor;
    this.emailServiceFactory = emailServiceFactory;
  }

  @Override
  public Map<String, Object> retrieveCreateParams(
      String integrationType, Map<String, Object> integrationParams) {
    BusinessRule.expect(integrationParams, MapUtils::isNotEmpty)
        .verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

    Map<String, Object> resultParams =
        Maps.newHashMapWithExpectedSize(EmailSettingsEnum.values().length);

    Optional<String> fromAttribute = EmailSettingsEnum.FROM.getAttribute(integrationParams);

    fromAttribute.ifPresent(from -> resultParams.put(EmailSettingsEnum.FROM.getAttribute(), from));

    ofNullable(integrationParams.get(EmailSettingsEnum.PORT.getAttribute()))
        .ifPresent(
            p -> {
              int port = IntegerUtils.parseInt(String.valueOf(p), -1);
              if ((port <= 0) || (port > 65535)) {
                BusinessRule.fail()
                    .withError(
                        ErrorType.INCORRECT_REQUEST,
                        "Incorrect 'Port' value. Allowed value is [1..65535]");
              }
              resultParams.put(EmailSettingsEnum.PORT.getAttribute(), p);
            });

    EmailSettingsEnum.PROTOCOL
        .getAttribute(integrationParams)
        .ifPresent(
            protocol -> resultParams.put(EmailSettingsEnum.PROTOCOL.getAttribute(), protocol));

    EmailSettingsEnum.USERNAME
        .getAttribute(integrationParams)
        .ifPresent(
            username -> resultParams.put(EmailSettingsEnum.USERNAME.getAttribute(), username));

    ofNullable(integrationParams.get(EmailSettingsEnum.AUTH_ENABLED.getAttribute()))
        .ifPresent(
            authEnabledAttribute -> {
              boolean isAuthEnabled = BooleanUtils.toBoolean(String.valueOf(authEnabledAttribute));
              if (isAuthEnabled) {
                EmailSettingsEnum.PASSWORD
                    .getAttribute(integrationParams)
                    .ifPresent(
                        password ->
                            resultParams.put(
                                EmailSettingsEnum.PASSWORD.getAttribute(),
                                basicTextEncryptor.encrypt(password)));
              } else {
                /* Auto-drop values on switched-off authentication */
                resultParams.put(EmailSettingsEnum.PASSWORD.getAttribute(), null);
              }
              resultParams.put(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), isAuthEnabled);
            });

    EmailSettingsEnum.STAR_TLS_ENABLED
        .getAttribute(integrationParams)
        .ifPresent(
            attr ->
                resultParams.put(
                    EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute(),
                    BooleanUtils.toBoolean(attr)));
    EmailSettingsEnum.SSL_ENABLED
        .getAttribute(integrationParams)
        .ifPresent(
            attr ->
                resultParams.put(
                    EmailSettingsEnum.SSL_ENABLED.getAttribute(), BooleanUtils.toBoolean(attr)));
    EmailSettingsEnum.HOST
        .getAttribute(integrationParams)
        .ifPresent(attr -> resultParams.put(EmailSettingsEnum.HOST.getAttribute(), attr));
    EmailSettingsEnum.RP_HOST
        .getAttribute(integrationParams)
        .filter(UrlValidator.getInstance()::isValid)
        .ifPresent(attr -> resultParams.put(EmailSettingsEnum.RP_HOST.getAttribute(), attr));

    return resultParams;
  }

  @Override
  public Map<String, Object> retrieveUpdatedParams(
      String integrationType, Map<String, Object> integrationParams) {
    return retrieveCreateParams(integrationType, integrationParams);
  }

  @Override
  public Integration createIntegration(IntegrationRQ integrationRq,
      IntegrationType integrationType) {
    Integration integration = super.createIntegration(integrationRq, integrationType);
    sendConnectionTestEmail(integration, true);
    return integration;
  }

  @Override
  public Integration updateIntegration(Integration integration, IntegrationRQ integrationRq) {
    Integration updatedIntegration = super.updateIntegration(integration, integrationRq);
    sendConnectionTestEmail(updatedIntegration, false);
    return updatedIntegration;
  }

  @Override
  public boolean checkConnection(Integration integration) {
    return emailServiceFactory.getEmailService(integration).map(emailService -> {
      try {
        emailService.testConnection();
      } catch (MessagingException ex) {
        LOGGER.error("Connection to email server failed", ex);
        fail()
            .withError(
                EMAIL_CONFIGURATION_IS_INCORRECT,
                "Email configuration is incorrect. Please, check your configuration. "
                    + ex.getMessage());
        return false;
      }
      return true;
    }).orElse(false);
  }

  private void sendConnectionTestEmail(Integration integration, boolean isNewIntegration) {
    boolean isAuthEnabled = BooleanUtils.toBoolean(
        EmailSettingsEnum.AUTH_ENABLED
            .getAttribute(integration.getParams().getParams())
            .orElse("false"));
    emailServiceFactory.getEmailService(integration).ifPresent(emailService -> {
      if (isAuthEnabled) {
        try {
          emailService.sendConnectionTestEmail(isNewIntegration);
        } catch (MessagingException ex) {
          LOGGER.error("Cannot send email to user", ex);
          fail()
              .withError(
                  EMAIL_CONFIGURATION_IS_INCORRECT,
                  "Email configuration is incorrect. Please, check your configuration. "
                      + ex.getMessage());
        }
      }
    });
  }
}