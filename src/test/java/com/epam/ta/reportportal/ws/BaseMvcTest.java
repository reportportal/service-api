package com.epam.ta.reportportal.ws;

import com.epam.ta.reportportal.TestConfig;
import com.epam.ta.reportportal.auth.OAuthHelper;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
@ContextConfiguration(classes = TestConfig.class)
@TestExecutionListeners(listeners = { FlywayTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
public abstract class BaseMvcTest {

	protected static final String DEFAULT_PROJECT_BASE_URL = "/default_personal";
	protected static final String SUPERADMIN_PROJECT_BASE_URL = "/superadmin_personal";

	@Autowired
	protected OAuthHelper oAuthHelper;

	@Autowired
	protected MockMvc mockMvc;

	@MockBean
	protected MessageBus messageBus;

	@MockBean
	protected MailServiceFactory mailServiceFactory;

	@Mock
	protected EmailService emailService;

	@FlywayTest
	@BeforeClass
	public static void before() {
	}

	protected RequestPostProcessor token(OAuth2AccessToken token) {
		return mockRequest -> {
			mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
			return mockRequest;
		};
	}

}
