package com.epam.ta.reportportal.ws;

import com.epam.ta.reportportal.TestConfig;
import com.epam.ta.reportportal.core.events.MessageBus;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
@ContextConfiguration(classes = TestConfig.class)
public abstract class BaseMvcTest {

	@Autowired
	protected MockMvc mockMvc;

	@MockBean
	protected MessageBus messageBus;
}
