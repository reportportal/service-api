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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClientTemplate;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.ANALYZER_KEY;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@EnableRabbit
@Configuration
public class AnalyzerRabbitMqConfiguration {

	@Autowired
	private MessageConverter messageConverter;

	@Bean
	public RabbitMqManagementClient managementTemplate(@Value("${rp.amqp.api-address}") String address) {
		Client rabbitClient;
		try {
			rabbitClient = new Client(address);
		} catch (Exception e) {
			throw new ReportPortalException(
					ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
					"Cannot create a HTTP rabbit client instance. Incorrect api address " + address
			);
		}
		return new RabbitMqManagementClientTemplate(rabbitClient);
	}

	@Bean(name = "analyzerConnectionFactory")
	public ConnectionFactory analyzerConnectionFactory(@Value("${rp.amqp.addresses}") URI addresses) {
		CachingConnectionFactory factory = new CachingConnectionFactory(addresses);
		factory.setVirtualHost(ANALYZER_KEY);
		return factory;
	}

	@Bean(name = "analyzerRabbitTemplate")
	public RabbitTemplate analyzerRabbitTemplate(@Autowired @Qualifier("analyzerConnectionFactory") ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

}
