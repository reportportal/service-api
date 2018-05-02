/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.core.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Bortnik
 */
@EnableRabbit
@Configuration
public class RabbitMqConfiguration {

	public static final String EXCHANGE_KEY = "reporting-exchange-1";
	public static final String START_REPORTING_QUEUE = "start-reporting-queue";
	public static final String FINISH_REPORTING_QUEUE = "finish-reporting-queue";
	public static final String START_ROUTING_KEY = "start";
	public static final String FINISH_ROUTING_KEY = "finish";

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		return new CachingConnectionFactory("localhost");
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setChannelTransacted(true);
		return rabbitTemplate;
	}

	@Bean
	public DirectExchange directExchange() {
		return new DirectExchange(EXCHANGE_KEY);
	}

	@Bean
	public Queue startReportingQueue() {
		return new Queue(START_REPORTING_QUEUE);
	}

	@Bean
	public Queue finishReportingQueue() {
		return new Queue(FINISH_REPORTING_QUEUE);
	}

	@Bean
	public Binding startReportingBinding() {
		return BindingBuilder.bind(startReportingQueue()).to(directExchange()).with(START_ROUTING_KEY);
	}

	@Bean
	public Binding finishReportingBinding() {
		return BindingBuilder.bind(finishReportingQueue()).to(directExchange()).with(FINISH_ROUTING_KEY);
	}

}
