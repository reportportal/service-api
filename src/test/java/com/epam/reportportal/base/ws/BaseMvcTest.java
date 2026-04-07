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

package com.epam.reportportal.base.ws;

import com.epam.reportportal.base.TestConfig;
import com.epam.reportportal.base.auth.OAuthHelper;
import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.base.core.events.MessageBus;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.util.BinaryDataResponseWriter;
import com.epam.reportportal.base.util.email.EmailService;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Slf4j
@ApplicationModuleTest(module = "base")
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
@ContextConfiguration(classes = TestConfig.class)
@TestExecutionListeners(listeners = {
    FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
@RecordApplicationEvents
public abstract class BaseMvcTest {

  protected static final String DEFAULT_PROJECT_BASE_URL = "/v1/default_personal";
  protected static final String SUPERADMIN_PROJECT_BASE_URL = "/v1/superadmin_personal";

  protected String adminToken;
  protected String managerToken;
  protected String editorToken;
  protected String viewerToken;
  protected String noOrgUser;
  protected String noProjectsUser;

  @Autowired
  protected OAuthHelper oAuthHelper;

  @Autowired
  protected MockMvc mockMvc;

  @MockBean
  protected MessageBus messageBus;

  @MockBean
  protected MailServiceFactory mailServiceFactory;

  @MockBean
  protected Pf4jPluginBox pluginBox;

  @MockBean(name = "pluginFilesProvider")
  protected PluginFilesProvider pluginFilesProvider;

  @MockBean(name = "pluginPublicFilesProvider")
  protected PluginFilesProvider pluginPublicFilesProvider;

  @MockBean
  protected BinaryDataResponseWriter binaryDataResponseWriter;

  @MockBean
  protected ExecuteIntegrationHandler executeIntegrationHandler;

  @MockBean
  protected CreateAuthIntegrationHandler createAuthIntegrationHandler;

  @Mock
  protected BtsExtension extension;

  @Mock
  protected EmailService emailService;

  @FlywayTest(invokeCleanDB = false)
  @BeforeAll
  public static void before() {
  }

  @BeforeEach
  void beforeEach() {
    adminToken = oAuthHelper.createAccessToken("admin@example.com", "erebus",
        UserRole.ADMINISTRATOR);
    managerToken = oAuthHelper.createAccessToken("user-manager@example.com", "erebus",
        UserRole.USER);
    editorToken = oAuthHelper.createAccessToken("user-member-editor@example.com", "erebus",
        UserRole.USER);
    viewerToken = oAuthHelper.createAccessToken("user-member-viewer@example.com", "erebus",
        UserRole.USER);
    noProjectsUser = oAuthHelper.createAccessToken("no-projects-user@example.com", "erebus",
        UserRole.USER);
    noOrgUser = oAuthHelper.createAccessToken("no-orgs-user@example.com", "erebus", UserRole.USER);
  }

  protected RequestPostProcessor token(String tokenValue) {
    return mockRequest -> {
      mockRequest.addHeader("Authorization", "Bearer " + tokenValue);
      return mockRequest;
    };
  }

}
