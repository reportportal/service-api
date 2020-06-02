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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.impl.RabbitMqManagementClientTemplate;
import com.epam.ta.reportportal.core.events.handler.IntegrationSecretsMigrationHandler;
import com.epam.ta.reportportal.util.ApplicationContextAwareFactoryBeanTest;
import com.epam.ta.reportportal.util.ResourceCopierBeanTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
@EnableAutoConfiguration(exclude = { QuartzAutoConfiguration.class, RabbitAutoConfiguration.class })
@ComponentScan(value = { "com.epam.ta.reportportal" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.ws.rabbit.*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = { "com.epam.ta.reportportal.job.*" }),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = { "com.epam.ta.reportportal.core.integration.migration.*" }),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ResourceCopierBeanTest.TestConfig.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = IntegrationSecretsMigrationHandler.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApplicationContextAwareFactoryBeanTest.TestConfig.class) })
public class TestConfig {

	@MockBean
	protected Client rabbitClient;

	@MockBean(name = "analyzerRabbitTemplate")
	protected RabbitTemplate analyzerRabbitTemplate;

	@MockBean(name = "rabbitTemplate")
	protected RabbitTemplate rabbitTemplate;

	@MockBean
	protected MessageConverter messageConverter;

	@Autowired
	private DatabaseUserDetailsService userDetailsService;

	@Bean
	@Profile("unittest")
	protected RabbitMqManagementClient managementTemplate() {
		return new RabbitMqManagementClientTemplate(rabbitClient);
	}

	@Bean
	@Profile("unittest")
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();
		jwtConverter.setSigningKey("123");

		DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
		DefaultUserAuthenticationConverter defaultUserAuthenticationConverter = new DefaultUserAuthenticationConverter();
		defaultUserAuthenticationConverter.setUserDetailsService(userDetailsService);
		accessTokenConverter.setUserTokenConverter(defaultUserAuthenticationConverter);

		jwtConverter.setAccessTokenConverter(accessTokenConverter);

		return jwtConverter;
	}

	@Bean
	public ObjectMapper testObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		objectMapper.registerModule(new JavaTimeModule());

		return objectMapper;
	}
}