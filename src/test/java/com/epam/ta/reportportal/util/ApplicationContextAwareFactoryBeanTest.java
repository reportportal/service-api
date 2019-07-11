package com.epam.ta.reportportal.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationContextAwareFactoryBeanTest.TestConfig.class })
public class ApplicationContextAwareFactoryBeanTest {

	@Autowired
	private ApplicationContextAwareFactoryBeanTest testObject;

	@Autowired
	private ApplicationContext context;

	@Test
	void testSingleton() {
		assertThat(testObject, is(context.getBean(ApplicationContextAwareFactoryBeanTest.class)));
	}

	@Configuration
	public static class TestConfig {

		@Bean
		FactoryBean<ApplicationContextAwareFactoryBeanTest> resourceCopier() {
			return new ApplicationContextAwareFactoryBean<ApplicationContextAwareFactoryBeanTest>() {

				@Override
				public Class<?> getObjectType() {
					return ApplicationContextAwareFactoryBeanTest.class;
				}

				@Override
				protected ApplicationContextAwareFactoryBeanTest createInstance() {
					return new ApplicationContextAwareFactoryBeanTest();
				}
			};
		}
	}
}