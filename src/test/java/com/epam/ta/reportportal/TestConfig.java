/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.client.impl.RabbitMqManagementClientTemplate;
import com.epam.ta.reportportal.job.SaveBinaryDataJob;
import com.epam.ta.reportportal.util.ApplicationContextAwareFactoryBeanTest;
import com.epam.ta.reportportal.util.ResourceCopierBeanTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
@EnableAutoConfiguration(exclude = QuartzAutoConfiguration.class)
@ComponentScan(value = "com.epam.ta.reportportal", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.ws.rabbit.*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.job.*"),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ResourceCopierBeanTest.TestConfig.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApplicationContextAwareFactoryBeanTest.TestConfig.class) }, includeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SaveBinaryDataJob.class) })
@PropertySource("classpath:test-application.properties")
public class TestConfig {

	@MockBean
	protected Client rabbitClient;

	@MockBean(name = "analyzerRabbitTemplate")
	protected RabbitTemplate rabbitTemplate;

	@Bean
	@Profile("unittest")
	protected RabbitMqManagementClient managementTemplate() {
		return new RabbitMqManagementClientTemplate(rabbitClient);
	}

	@Bean
	public ObjectMapper testObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		objectMapper.registerModule(new JavaTimeModule());

		return objectMapper;
	}
}