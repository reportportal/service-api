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
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Pavel Bortnik
 */
@EnableRabbit
@Configuration
public class RabbitMqConfiguration {

	public static final String START_LAUNCH_QUEUE = "start-launch";
	public static final String FINISH_LAUNCH_QUEUE = "finish-launch";
	public static final String START_ITEM_QUEUE = "start-parent-item";
	public static final String START_CHILD_QUEUE = "start-child-item";
	public static final String FINISH_ITEM_QUEUE = "finish-item";

	public static final String QUERY_RQ_QUEUE = "query-rq-queue";

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
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(PlatformTransactionManager transactionManager) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setDefaultRequeueRejected(false);
		factory.setTransactionManager(transactionManager);
		factory.setChannelTransacted(true);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setConcurrentConsumers(3);
		factory.setMaxConcurrentConsumers(10);
		return factory;
	}

	@Bean
	public Queue startLaunchQueue() {
		return new Queue(START_LAUNCH_QUEUE);
	}

	@Bean
	public Queue finishLaunchQueue() {
		return new Queue(FINISH_LAUNCH_QUEUE);
	}

	@Bean
	public Queue startItemQueue() {
		return new Queue(START_ITEM_QUEUE);
	}

	@Bean
	public Queue startChildQueue() {
		return new Queue(START_CHILD_QUEUE);
	}

	@Bean
	public Queue finishItemQueue() {
		return new Queue(FINISH_ITEM_QUEUE);
	}

	@Bean
	public Queue queryQueue() {
		return new Queue(QUERY_RQ_QUEUE);
	}

	@Bean
	public RabbitTemplate amqpTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

}
