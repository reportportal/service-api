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

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.MessageBusImpl;
import com.epam.ta.reportportal.core.plugin.RabbitAwarePluginBox;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Service;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * @author Pavel Bortnik
 */
@EnableRabbit
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class RabbitMqConfiguration {

    public static final long DEAD_LETTER_DELAY_MILLIS = 3_000L;
	public static final long DEAD_LETTER_MAX_RETRY = 5L;

	/**
	 * Exchanges
	 */
	public static final String EXCHANGE_EVENTS = "broadcast.events";
	public static final String EXCHANGE_ACTIVITY = "direct.activity";
	public static final String EXCHANGE_PLUGINS = "plugins";
	public static final String EXCHANGE_REPORTING = "reporting";
	public static final String EXCHANGE_ATTACHMENT = "direct.attachment";
	public static final String EXCHANGE_DLQ = "dead.letter";

	/**
	 * Queues
	 */
	public static final String KEY_EVENTS = "broadcast.events";
	public static final String QUEUE_ACTIVITY = "activity";
	public static final String QUEUE_ATTACHMENT_DELETE = "attachment.delete";
	public static final String QUEUE_LAUNCH_START = "reporting.launch.start";
	public static final String QUEUE_LAUNCH_FINISH = "reporting.launch.finish";
	public static final String QUEUE_LAUNCH_STOP = "reporting.launch.stop";
	public static final String QUEUE_LAUNCH_BULK_STOP = "reporting.launch.bulkStop";
	public static final String QUEUE_ITEM_START = "reporting.item.start";
	public static final String QUEUE_ITEM_FINISH = "reporting.item.finish";

	public static final String LOGS_FIND_BY_TEST_ITEM_REF_QUEUE = "repository.find.logs.by.item";
	public static final String DATA_STORAGE_FETCH_DATA_QUEUE = "repository.find.data";
	public static final String TEST_ITEMS_FIND_ONE_QUEUE = "repository.find.item";
	public static final String INTEGRATION_FIND_ONE = "repository.find.integration";
	public static final String PROJECTS_FIND_BY_NAME = "repository.find.project.by.name";

	public static final String KEY_PLUGINS_PING = "broadcast.plugins.ping";
	public static final String KEY_PLUGINS_PONG = "broadcast.plugins.pong";

	public static final String QUEUE_QUERY_RQ = "query-rq";

	/**
	 * Dead letter queues
	 * Could be all messages in single DLQ, but better keep them by kind
	 */
	public static final String QUEUE_ITEM_START_DLQ = "reporting.item.start.dlq";
	public static final String QUEUE_ITEM_FINISH_DLQ = "reporting.item.finish.dlq";
	public static final String QUEUE_LAUNCH_FINISH_DLQ = "reporting.launch.finish.dlq";
	public static final String QUEUE_LAUNCH_STOP_DLQ = "reporting.launch.stop.dlq";
	public static final String QUEUE_LAUNCH_BULK_STOP_DLQ = "reporting.launch.bulkStop.dlq";



	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public Service pluginBox(@Autowired MessageBus messageBus) {
		Service service = new RabbitAwarePluginBox(messageBus).startAsync();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> service.stopAsync().awaitTerminated()));
		return service;
	}

	@Bean
	public MessageBus messageBus(@Autowired @Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
		return new MessageBusImpl(amqpTemplate);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter(objectMapper);
	}

	@Bean
	public ConnectionFactory connectionFactory(@Value("${rp.amqp.addresses}") URI addresses) {
		return new CachingConnectionFactory(addresses);
	}

	@Bean
	public AmqpAdmin amqpAdmin(@Autowired ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(@Autowired ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setDefaultRequeueRejected(false);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setConcurrentConsumers(1);
		factory.setMaxConcurrentConsumers(1);
		return factory;
	}

	@Bean(name = "rabbitTemplate")
	public RabbitTemplate rabbitTemplate(@Autowired @Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}



	/* Exchanges definition */


	@Bean
	public FanoutExchange eventsExchange() {
		return new FanoutExchange(EXCHANGE_EVENTS, false, false);
	}

	@Bean
	public TopicExchange pluginsExchange() {
		return new TopicExchange(EXCHANGE_PLUGINS, false, false);
	}

	@Bean
	public DirectExchange activityExchange() {
		return new DirectExchange(EXCHANGE_ACTIVITY, true, false);
	}

	@Bean
	public DirectExchange reportingExchange() {
		return new DirectExchange(EXCHANGE_REPORTING, true, false);
	}

	@Bean
	public DirectExchange attachmentExchange() {
		return new DirectExchange(EXCHANGE_ATTACHMENT, true, false);
	}

	@Bean
	public DirectExchange deadLetterExchange() {
		return new DirectExchange(EXCHANGE_DLQ, true, false);
	}



	/* Queues definition */


	@Bean
	public Queue launchStartQueue() {
		return new Queue(QUEUE_LAUNCH_START);
	}

	@Bean
	public Queue launchFinishQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_FINISH)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_FINISH_DLQ)
//				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchFinishDLQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_FINISH_DLQ)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_FINISH)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchStopQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_STOP)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_STOP_DLQ)
//				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchStopDLQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_STOP_DLQ)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_STOP)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchBulkStopQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_BULK_STOP)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_BULK_STOP_DLQ)
//				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchBulkStopDLQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_BULK_STOP_DLQ)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_BULK_STOP)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemStartQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_START)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_START_DLQ)
//				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemStartDLQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_START_DLQ)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_START)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemFinishQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_FINISH)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_FINISH_DLQ)
//				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemFinishDLQueue() {
        return QueueBuilder.durable(QUEUE_ITEM_FINISH_DLQ)
                .withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
                .withArgument("x-dead-letter-routing-key", QUEUE_ITEM_FINISH)
                .withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
                .build();
	}

//	@Bean
//	// Using stateless RetryOperationsInterceptor is not good approach, for it works through Thread.sleep()
//	// thus blocking thread on retry operations
//	RetryOperationsInterceptor interceptor() {
//		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
//		backOffPolicy.setBackOffPeriod(1000);
//
//		return RetryInterceptorBuilder.stateless()
//				.backOffPolicy(backOffPolicy)
//				.maxAttempts(5)
//				.build();
//	}

	@Bean
	public Queue pluginsPongQueue() {
		return new AnonymousQueue(new AnonymousQueue.Base64UrlNamingStrategy(KEY_PLUGINS_PONG + "."));
	}

	@Bean
	public Queue pluginsPingQueue() {
		return new AnonymousQueue(new AnonymousQueue.Base64UrlNamingStrategy(KEY_PLUGINS_PING + "."));
	}

	@Bean
	public Queue eventsQueue() {
		return new AnonymousQueue(new AnonymousQueue.Base64UrlNamingStrategy(KEY_EVENTS + "."));
	}

	@Bean
	public Queue activityQueue() {
		return new Queue(QUEUE_ACTIVITY);
	}

	@Bean
	public Queue deleteAttachmentQueue() {
		return new Queue(QUEUE_ATTACHMENT_DELETE);
	}


	@Bean
	public Queue projectRepoQueue() {
		return new Queue(PROJECTS_FIND_BY_NAME);
	}

	@Bean
	public Queue dataStorageQueue() {
		return new Queue(DATA_STORAGE_FETCH_DATA_QUEUE);
	}

	@Bean
	public Queue integrationRepoQueue() {
		return new Queue(INTEGRATION_FIND_ONE);
	}

	@Bean
	public Queue logRepoQueue() {
		return new Queue(LOGS_FIND_BY_TEST_ITEM_REF_QUEUE);
	}

	@Bean
	public Queue testItemRepoQueue() {
		return new Queue(TEST_ITEMS_FIND_ONE_QUEUE);
	}

	@Bean
	public Queue queryQueue() {
		return new Queue(QUEUE_QUERY_RQ);
	}



	/* Bindings */


	@Bean
	public Binding launchStartBinding() {
		return BindingBuilder.bind(launchStartQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_START);
	}

	@Bean
	public Binding launchFinishBinding() {
		return BindingBuilder.bind(launchFinishQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_FINISH);
	}

	@Bean
	public Binding launchStopBinding() {
		return BindingBuilder.bind(launchStopQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_STOP);
	}

	@Bean
	public Binding launchBulkStopBinding() {
		return BindingBuilder.bind(launchBulkStopQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_BULK_STOP);
	}

	@Bean
	public Binding itemRootStartBinding() {
		return BindingBuilder.bind(itemStartQueue()).to(reportingExchange()).with(QUEUE_ITEM_START);
	}

	@Bean
	public Binding itemFinishBinding() {
		return BindingBuilder.bind(itemFinishQueue()).to(reportingExchange()).with(QUEUE_ITEM_FINISH);
	}

	@Bean
	public Binding pluginsPongBinding() {
		return BindingBuilder.bind(pluginsPongQueue()).to(pluginsExchange()).with(KEY_PLUGINS_PONG);
	}

	@Bean
	public Binding eventsQueueBinding() {
		return BindingBuilder.bind(eventsQueue()).to(eventsExchange());
	}

	@Bean
	public Binding eventsActivityBinding() {
		return BindingBuilder.bind(activityQueue()).to(activityExchange()).with(QUEUE_ACTIVITY);
	}

//	@Bean
//	public Binding pluginsPingBinding() {
//		return BindingBuilder.bind(pluginsPingQueue()).to(pluginsExchange()).with(KEY_PLUGINS_PING);
//	}

	@Bean
	public Binding attachmentDeleteBinding() {
		return BindingBuilder.bind(deleteAttachmentQueue()).to(attachmentExchange()).with(QUEUE_ATTACHMENT_DELETE);
	}

	@Bean
	public Binding launchFinishDLQBinding() {
		return BindingBuilder.bind(launchFinishDLQueue()).to(deadLetterExchange()).with(QUEUE_LAUNCH_FINISH_DLQ);
	}

	@Bean
	public Binding launchStopDLQBinding() {
		return BindingBuilder.bind(launchStopDLQueue()).to(deadLetterExchange()).with(QUEUE_LAUNCH_STOP_DLQ);
	}

	@Bean
	public Binding launchBulkStopDLQBinding() {
		return BindingBuilder.bind(launchBulkStopDLQueue()).to(deadLetterExchange()).with(QUEUE_LAUNCH_BULK_STOP_DLQ);
	}

	@Bean
	public Binding itemStartDLQBinding() {
		return BindingBuilder.bind(itemStartDLQueue()).to(deadLetterExchange()).with(QUEUE_ITEM_START_DLQ);
	}

	@Bean
	public Binding itemFinishDLQBinding() {
		return BindingBuilder.bind(itemFinishDLQueue()).to(deadLetterExchange()).with(QUEUE_ITEM_FINISH_DLQ);
	}

}
