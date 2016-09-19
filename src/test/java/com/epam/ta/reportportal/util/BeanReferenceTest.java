/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;

/**
 * BeanReferenceTest
 *
 * @author Andrei Varabyeu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BeanReferenceTest.TestConfiguration.class })
public class BeanReferenceTest {

	@Autowired
	private LazyReference<DemoBean> beanReference;

	@Autowired
	private LazyReference<DemoBean> lazyBeanReference;

	@Test
	public void testBeanReference() {
		DemoBean bean1 = beanReference.get();
		DemoBean bean2 = beanReference.get();

		Assert.assertThat(bean1.getBeanField(), not(equalTo(bean2.getBeanField())));
	}


	private static class DemoBean {

		private String beanField;

		private DemoBean() {
			beanField = RandomStringUtils.randomAlphabetic(10);
		}

		public String getBeanField() {
			return beanField;
		}
	}

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public LazyReference<DemoBean> beanReference() {
			return new LazyReference<>(DemoBean.class);
		}



		@Bean
		@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public DemoBean demoBean() {
			return new DemoBean();
		}
	}
}