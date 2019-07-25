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

package com.epam.ta.reportportal.ws;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.TestConfig;
import com.epam.ta.reportportal.auth.OAuthHelper;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
@ContextConfiguration(classes = TestConfig.class)
@TestExecutionListeners(listeners = { FlywayTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
public abstract class BaseMvcTest {

	protected static final String DEFAULT_PROJECT_BASE_URL = "/v1/default_personal";
	protected static final String SUPERADMIN_PROJECT_BASE_URL = "/v1/superadmin_personal";

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

	@Mock
	protected BtsExtension extension;

	@Mock
	protected EmailService emailService;

	@FlywayTest
	@BeforeAll
	public static void before() {
	}

	protected RequestPostProcessor token(String tokenValue) {
		return mockRequest -> {
			mockRequest.addHeader("Authorization", "Bearer " + tokenValue);
			return mockRequest;
		};
	}

}
