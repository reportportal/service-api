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

import com.epam.ta.reportportal.core.plugin.RabbitAwarePluginBox;
import com.google.common.util.concurrent.Service;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Pavel Bortnik
 */
@EnableRabbit
@Configuration
public class RabbitMqConfiguration {

	/**
	 * Exchanges
	 */
	public static final String EXCHANGE_EVENTS = "broadcast.events";
	public static final String EXCHANGE_PLUGINS = "plugins";
	public static final String EXCHANGE_REPORTING = "reporting";

	public static final String KEY_PLUGINS_PING = "broadcast.plugins-ping";
	public static final String KEY_PLUGINS_PONG = "broadcast.plugins-pong";

	/**
	 * Queues
	 */
	public static final String QUEUE_START_LAUNCH = "reporting.start-launch";
	public static final String QUEUE_FINISH_LAUNCH = "reporting.finish-launch";
	public static final String QUEUE_START_ITEM = "reporting.start-parent-item";
	public static final String QUEUE_FINISH_ITEM = "reporting.finish-item";

	@Bean
	public Service pluginBox(@Autowired AmqpTemplate amqpTemplate) {
		return new RabbitAwarePluginBox(amqpTemplate).startAsync();
	}

	public static final String QUEUE_QUERY_RQ = "query-rq";

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public ConnectionFactory connectionFactory(@Value("${spring.rabbitmq.host}") String host, @Value("${spring.rabbitmq.port}") Integer port) {
		return new CachingConnectionFactory(host, port);
	}

	@Bean
	public AmqpAdmin amqpAdmin(@Autowired ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(@Autowired ConnectionFactory connectionFactory,
			PlatformTransactionManager transactionManager) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
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
		return new Queue(QUEUE_START_LAUNCH);
	}

	@Bean
	public Queue finishLaunchQueue() {
		return new Queue(QUEUE_FINISH_LAUNCH);
	}

	@Bean
	public Queue startItemQueue() {
		return new Queue(QUEUE_START_ITEM);
	}

	@Bean
	public Queue finishItemQueue() {
		return new Queue(QUEUE_FINISH_ITEM);
	}

	@Bean
	public Queue pluginsPongQueue() {
		return new AnonymousQueue(new AnonymousQueue.Base64UrlNamingStrategy(KEY_PLUGINS_PONG + "."));
	}

	@Bean
	public Queue pluginsPingQueue() {
		return new AnonymousQueue(new AnonymousQueue.Base64UrlNamingStrategy(KEY_PLUGINS_PING + "."));
	}

	@Bean
	public FanoutExchange eventsExchange() {
		return new FanoutExchange(EXCHANGE_EVENTS, false, false);
	}

	@Bean
	public TopicExchange pluginsExchange() {
		return new TopicExchange(EXCHANGE_PLUGINS, false, false);
	}

	@Bean
	public DirectExchange reportingExchange() {
		return new DirectExchange(EXCHANGE_REPORTING, true, false);
	}

	@Bean
	public Binding startLaunchBinding() {
		return BindingBuilder.bind(startLaunchQueue()).to(reportingExchange()).with(QUEUE_START_LAUNCH);
	}

	@Bean
	public Binding finishLaunchBinding() {
		return BindingBuilder.bind(finishLaunchQueue()).to(reportingExchange()).with(QUEUE_FINISH_LAUNCH);
	}

	@Bean
	public Binding startItemBinding() {
		return BindingBuilder.bind(startItemQueue()).to(reportingExchange()).with(QUEUE_START_ITEM);
	}

	@Bean
	public Binding finishItemBinding() {
		return BindingBuilder.bind(finishItemQueue()).to(reportingExchange()).with(QUEUE_FINISH_ITEM);
	}

	@Bean
	public Binding pluginsPongBinding() {
		return BindingBuilder.bind(pluginsPongQueue()).to(pluginsExchange()).with(KEY_PLUGINS_PONG);
	}

	@Bean
	public Binding pluginsPingBinding() {
		return BindingBuilder.bind(pluginsPingQueue()).to(pluginsExchange()).with(KEY_PLUGINS_PING);
	}

	@Bean
	public Queue queryQueue() {
		return new Queue(QUEUE_QUERY_RQ);
	}

	@Bean
	public RabbitTemplate amqpTemplate(@Autowired ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public AsyncRabbitTemplate asyncAmqpTemplate(@Autowired RabbitTemplate rabbitTemplate) {
		return new AsyncRabbitTemplate(rabbitTemplate);
	}

}
