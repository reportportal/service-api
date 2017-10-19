/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;

/**
 * Checks {@link ApplicationContextAwareFactoryBean} <br>
 * Be aware that test uses yourself as demo bean
 *
 * @author Andrei Varabyeu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationContextAwareFactoryBeanTest.TestConfig.class })
public class ApplicationContextAwareFactoryBeanTest {

	@Autowired
	private ApplicationContextAwareFactoryBeanTest testObject;

	@Autowired
	private ApplicationContext context;

	@Test
	public void testSingleton() {
		Assert.assertThat(testObject, is(context.getBean(ApplicationContextAwareFactoryBeanTest.class)));
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