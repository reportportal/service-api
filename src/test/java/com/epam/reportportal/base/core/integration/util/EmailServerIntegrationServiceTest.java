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

package com.epam.reportportal.base.core.integration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.email.EmailService;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import java.net.ConnectException;
import java.util.stream.Stream;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import jakarta.mail.AuthenticationFailedException;
import org.mockito.ArgumentMatchers;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class EmailServerIntegrationServiceTest {

  private static final String INTEGRATION_NAME = "email";

  private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final PluginBox pluginBox = mock(PluginBox.class);
  private MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
  private EmailService emailService = mock(EmailService.class);
  private final IntegrationParamsEncryptor paramsEncryptorMock = mock(IntegrationParamsEncryptor.class);

  private EmailServerIntegrationService emailServerIntegrationService;

  @BeforeEach
  void setUp() {
    emailServerIntegrationService = new EmailServerIntegrationService(integrationRepository,
        pluginBox,
        paramsEncryptorMock,
        mailServiceFactory
    );
  }

  @Test
  void validateGlobalIntegrationNegative() throws MessagingException {
    //given
    Integration integration = new Integration();
    IntegrationType integrationType = new IntegrationType();
    integrationType.setName("email");
    integration.setType(integrationType);

    //when
    when(integrationRepository.findAllGlobalByType(integrationType)).thenReturn(
        Lists.newArrayList());
    when(mailServiceFactory.getEmailService(integration)).thenReturn(Optional.of(emailService));
    doThrow(MessagingException.class).when(emailService).testConnection();

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.retrieveCreateParams("email", new HashMap<>())
    );

    //then
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'No integration params provided'",
        exception.getMessage()
    );
  }

  @Test
  void retrieveIntegrationParams() {
    Map<String, Object> map = emailServerIntegrationService.retrieveCreateParams("email",
        getParams());
    assertEquals(defaultParams(), map);
  }

  @Test
  void retrieveIntegrationParamsInvalidPort() {
    Map<String, Object> params = Maps.newHashMap();
    params.put("from", "from@mail.com");
    params.put("port", "123456789");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.retrieveCreateParams("email", params)
    );
    assertEquals("Incorrect Request. Incorrect 'Port' value. Allowed value is [1..65535]",
        exception.getMessage());
  }

  private Map<String, Object> defaultParams() {
    Map<String, Object> res = Maps.newHashMap();
    res.put("protocol", "value2");
    res.put("host", "value3");
    res.put("from", "from@mail.com");
    return res;
  }

  private Map<String, Object> getParams() {
    Map<String, Object> params = Maps.newHashMap();
    params.put("from", "from@mail.com");
    params.put("protocol", "value2");
    params.put("host", "value3");

    return params;
  }

  static Stream<Exception> mailFailures() {
    return Stream.of(
        new MailAuthenticationException("Authentication failed",
            new AuthenticationFailedException("535 5.7.8 Username and Password not accepted")),
        new MailSendException("Mail server connection failed; nested exception is jakarta.mail.MessagingException: "
            + "Could not connect to SMTP host: smtp.example.com, port: 25",
            new jakarta.mail.MessagingException("Could not connect to SMTP host", new ConnectException("refused"))),
        new jakarta.mail.internet.AddressException("Sender email address is not exist"));
  }

  private Integration emailIntegration() {
    Integration integration = new Integration();
    IntegrationType type = new IntegrationType();
    type.setName("email");
    integration.setType(type);
    integration.setEnabled(true);
    Map<String, Object> params = Maps.newHashMap();
    params.put("authEnabled", true);
    params.put("from", "from@mail.com");
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  private IntegrationRQ request() {
    IntegrationRQ rq = new IntegrationRQ();
    rq.setEnabled(true);
    rq.setName("email");
    Map<String, Object> params = Maps.newHashMap();
    params.put("authEnabled", true);
    params.put("from", "from@mail.com");
    rq.setIntegrationParams(params);
    return rq;
  }

  private void stubSendFailure(Exception failure) throws Exception {
    when(mailServiceFactory.getEmailService(any(Integration.class))).thenReturn(Optional.of(emailService));
    doThrow(failure).when(emailService).sendConnectionTestEmail(ArgumentMatchers.anyBoolean());
    doThrow(failure).when(emailService).testConnection();
  }

  private static void assertClientError(ReportPortalException ex) {
    assertEquals(ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT, ex.getErrorType());
    String message = ex.getMessage();
    assertTrue(message.contains("Please, check your configuration."), message);
    assertTrue(!message.contains("535") && !message.contains("smtp.example.com"), message);
  }

  @ParameterizedTest
  @MethodSource("mailFailures")
  void createIntegrationMapsMailFailureToConfigurationError(Exception failure) throws Exception {
    when(paramsEncryptorMock.encryptSensitiveFields(any())).thenAnswer(i -> i.getArgument(0));
    stubSendFailure(failure);
    IntegrationType type = new IntegrationType();
    type.setName("email");

    assertClientError(assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.createIntegration(request(), type)));
  }

  @ParameterizedTest
  @MethodSource("mailFailures")
  void updateIntegrationMapsMailFailureToConfigurationError(Exception failure) throws Exception {
    when(paramsEncryptorMock.encryptSensitiveFields(any())).thenAnswer(i -> i.getArgument(0));
    stubSendFailure(failure);

    assertClientError(assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.updateIntegration(emailIntegration(), request())));
  }

  @ParameterizedTest
  @MethodSource("mailFailures")
  void checkConnectionMapsMailFailureToConfigurationError(Exception failure) throws Exception {
    stubSendFailure(failure);

    assertClientError(assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.checkConnection(emailIntegration())));
  }

  @Test
  void checkConnectionMapsCheckedConnectFailureToConfigurationError() throws Exception {
    when(mailServiceFactory.getEmailService(any(Integration.class))).thenReturn(Optional.of(emailService));
    doThrow(new jakarta.mail.MessagingException("Could not connect to SMTP host", new ConnectException("refused")))
        .when(emailService).testConnection();

    var ex = assertThrows(ReportPortalException.class,
        () -> emailServerIntegrationService.checkConnection(emailIntegration()));
    assertClientError(ex);
    assertTrue(ex.getMessage().contains("Unable to connect to the mail server"), ex.getMessage());
  }

  @Test
  void authenticationFailureMessageNamesCredentials() {
    var ex = MailServiceFactory.toConfigurationError(new MailAuthenticationException("Authentication failed"));
    assertTrue(ex.getMessage().contains("Authentication failed. Check the username and password."), ex.getMessage());
  }
}
