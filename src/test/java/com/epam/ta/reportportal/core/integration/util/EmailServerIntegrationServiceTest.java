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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class EmailServerIntegrationServiceTest {

  private static final String INTEGRATION_NAME = "email";

  private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final PluginBox pluginBox = mock(PluginBox.class);
  private MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
  private EmailService emailService = mock(EmailService.class);

  private EmailServerIntegrationService emailServerIntegrationService;

  @BeforeEach
  void setUp() {
    BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
    basicTextEncryptor.setPassword("123");
    emailServerIntegrationService = new EmailServerIntegrationService(integrationRepository,
        pluginBox,
        basicTextEncryptor,
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
}