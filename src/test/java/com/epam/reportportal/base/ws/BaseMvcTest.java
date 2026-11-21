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

import com.epam.reportportal.auth.basic.LoginService;
import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.base.TestConfig;
import com.epam.reportportal.base.auth.OAuthHelper;
import com.epam.reportportal.base.core.events.MessageBus;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.handler.InstallMarketplacePluginHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.util.BinaryDataResponseWriter;
import com.epam.reportportal.base.util.email.EmailService;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.builtin.BuiltinCommandResolver;
import com.epam.reportportal.extension.builtin.email.EmailExtension;
import com.rabbitmq.http.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rabbitmq.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

  @MockitoBean
  protected MessageBus messageBus;

  @MockitoBean
  protected MailServiceFactory mailServiceFactory;

  @MockitoBean
  protected Pf4jPluginBox pluginBox;

  @MockitoBean(name = "pluginFilesProvider")
  protected PluginFilesProvider pluginFilesProvider;

  @MockitoBean(name = "pluginPublicFilesProvider")
  protected PluginFilesProvider pluginPublicFilesProvider;

  @MockitoBean
  protected BinaryDataResponseWriter binaryDataResponseWriter;

  @MockitoBean
  protected BuiltinCommandResolver builtinCommandResolver;

  @MockitoBean
  protected ExecuteIntegrationHandler executeIntegrationHandler;

  @MockitoBean
  protected EmailExtension emailExtension;

  @MockitoBean
  protected CreateAuthIntegrationHandler createAuthIntegrationHandler;

  @MockitoBean
  protected LoginService loginService;

  @MockitoBean(name = "rabbitClient")
  protected Client rabbitClient;

  @MockitoBean(name = "analyzerRabbitTemplate")
  protected RabbitTemplate analyzerRabbitTemplate;

  @MockitoBean(name = "rabbitTemplate")
  protected RabbitTemplate rabbitTemplate;

  @MockitoBean(name = "connectionFactory")
  protected ConnectionFactory connectionFactory;

  @MockitoBean(name = "simpleRabbitListenerContainerFactoryConfigurer")
  protected SimpleRabbitListenerContainerFactoryConfigurer simpleRabbitListenerContainerFactoryConfigurer;

  @MockitoBean(name = "amqpAdmin")
  protected AmqpAdmin amqpAdmin;

  @MockitoBean
  protected MessageConverter messageConverter;

  // Declared here, not in MarketplaceControllerTest: the @MockBean set is part of the context
  // cache key, so a subclass that adds its own forks the cache and starts a second
  // EmbeddedPostgres, which fails on the port the first one already holds.
  @MockitoBean
  protected MarketplaceClient marketplaceClient;

  @MockitoBean
  protected InstallMarketplacePluginHandler installMarketplacePluginHandler;

  @Mock
  protected ReportPortalExtensionPoint extension;

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
