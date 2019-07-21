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

package com.epam.ta.reportportal.core.configs.rabbit;

import com.epam.ta.reportportal.core.configs.Conditions;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantin Antipin
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class ReportingConfiguration {

	public static final long DEAD_LETTER_DELAY_MILLIS = 300_000L;
	public static final long DEAD_LETTER_MAX_RETRY = 300L;

	/**
	 * Exchanges
	 */
	public static final String EXCHANGE_REPORTING = "reporting";
	public static final String EXCHANGE_DLQ = "reporting.dlq";

	/**
	 * Queues
	 */
	public static final String QUEUE_LAUNCH_START = "reporting.launch.start";
	public static final String QUEUE_LAUNCH_FINISH = "reporting.launch.finish";
	public static final String QUEUE_ITEM_START = "reporting.item.start";
	public static final String QUEUE_ITEM_FINISH = "reporting.item.finish";
	public static final String QUEUE_LOG = "reporting.log";

	/**
	 * Retrying queues (using DLQ, x-message-ttl mechanism - see https://www.cloudamqp.com/docs/delayed-messages.html)
	 */
	public static final String QUEUE_LAUNCH_START_RETRY = "reporting.launch.start.retry";
	public static final String QUEUE_LAUNCH_FINISH_RETRY = "reporting.launch.finish.retry";
	public static final String QUEUE_ITEM_START_RETRY = "reporting.item.start.retry";
	public static final String QUEUE_ITEM_FINISH_RETRY = "reporting.item.finish.retry";
	public static final String QUEUE_LOG_RETRY = "reporting.log.retry";

	/**
	 * Dead letter queues (dropped messages go here)
	 */
	public static final String QUEUE_LAUNCH_START_DLQ = "reporting.launch.start.dlq";
	public static final String QUEUE_LAUNCH_FINISH_DLQ = "reporting.launch.finish.dlq";
	public static final String QUEUE_ITEM_START_DLQ = "reporting.item.start.dlq";
	public static final String QUEUE_ITEM_FINISH_DLQ = "reporting.item.finish.dlq";
	public static final String QUEUE_LOG_DLQ = "reporting.log.dlq";


	/**
	 * Exchanges definition
	 */

	@Bean
	public DirectExchange reportingExchange() {
		return new DirectExchange(EXCHANGE_REPORTING, true, false);
	}

	@Bean
	public DirectExchange reportingDeadLetterExchange() {
		return new DirectExchange(EXCHANGE_DLQ, true, false);
	}

	/**
	 * Queues definition
	 */
	@Bean
	public Queue launchStartQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_START)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_START_RETRY)
				.build();
	}

	@Bean
	public Queue launchStartRetryQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_START_RETRY)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_START)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchStartDLQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_START_DLQ)
				.build();
	}

	@Bean
	public Queue launchFinishQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_FINISH)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_FINISH_RETRY)
				.build();
	}

	@Bean
	public Queue launchFinishRetryQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_FINISH_RETRY)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LAUNCH_FINISH)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue launchFinishDLQueue() {
		return QueueBuilder.durable(QUEUE_LAUNCH_FINISH_DLQ)
				.build();
	}

	@Bean
	public Queue itemStartQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_START)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_START_RETRY)
				.build();
	}

	@Bean
	public Queue itemStartRetryQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_START_RETRY)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_START)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemStartDLQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_START_DLQ)
				.build();
	}

	@Bean
	public Queue itemFinishQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_FINISH)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_FINISH_RETRY)
				.build();
	}

	@Bean
	public Queue itemFinishRetryQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_FINISH_RETRY)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_ITEM_FINISH)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue itemFinishDLQueue() {
		return QueueBuilder.durable(QUEUE_ITEM_FINISH_DLQ)
				.build();
	}

	@Bean
	public Queue logQueue() {
		return QueueBuilder.durable(QUEUE_LOG)
				.withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
				.withArgument("x-dead-letter-routing-key", QUEUE_LOG_RETRY)
				.build();
	}

	@Bean
	public Queue logRetryQueue() {
		return QueueBuilder.durable(QUEUE_LOG_RETRY)
				.withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
				.withArgument("x-dead-letter-routing-key", QUEUE_LOG)
				.withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
				.build();
	}

	@Bean
	public Queue logDLQueue() {
		return QueueBuilder.durable(QUEUE_LOG_DLQ)
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

	/**
	 * Bindings
	 */

	@Bean
	public Binding launchStartBinding() {
		return BindingBuilder.bind(launchStartQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_START);
	}

	@Bean
	public Binding launchStartRetryBinding() {
		return BindingBuilder.bind(launchStartRetryQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LAUNCH_START_RETRY);
	}

	@Bean
	public Binding launchStartDLQBinding() {
		return BindingBuilder.bind(launchStartDLQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LAUNCH_START_DLQ);
	}

	@Bean
	public Binding launchFinishBinding() {
		return BindingBuilder.bind(launchFinishQueue()).to(reportingExchange()).with(QUEUE_LAUNCH_FINISH);
	}

	@Bean
	public Binding launchFinishRetryBinding() {
		return BindingBuilder.bind(launchFinishRetryQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LAUNCH_FINISH_RETRY);
	}

	@Bean
	public Binding launchFinishDLQBinding() {
		return BindingBuilder.bind(launchFinishDLQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LAUNCH_FINISH_DLQ);
	}

	@Bean
	public Binding itemStartBinding() {
		return BindingBuilder.bind(itemStartQueue()).to(reportingExchange()).with(QUEUE_ITEM_START);
	}

	@Bean
	public Binding itemStartRetryBinding() {
		return BindingBuilder.bind(itemStartRetryQueue()).to(reportingDeadLetterExchange()).with(QUEUE_ITEM_START_RETRY);
	}

	@Bean
	public Binding itemStartDLQBinding() {
		return BindingBuilder.bind(itemStartDLQueue()).to(reportingDeadLetterExchange()).with(QUEUE_ITEM_START_DLQ);
	}

	@Bean
	public Binding itemFinishBinding() {
		return BindingBuilder.bind(itemFinishQueue()).to(reportingExchange()).with(QUEUE_ITEM_FINISH);
	}

	@Bean
	public Binding itemFinishRetryBinding() {
		return BindingBuilder.bind(itemFinishRetryQueue()).to(reportingDeadLetterExchange()).with(QUEUE_ITEM_FINISH_RETRY);
	}

	@Bean
	public Binding itemFinishDLQBinding() {
		return BindingBuilder.bind(itemFinishDLQueue()).to(reportingDeadLetterExchange()).with(QUEUE_ITEM_FINISH_DLQ);
	}

	@Bean
	public Binding logBinding() {
		return BindingBuilder.bind(logQueue()).to(reportingExchange()).with(QUEUE_LOG);
	}

	@Bean
	public Binding logRetryBinding() {
		return BindingBuilder.bind(logRetryQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LOG_RETRY);
	}

	@Bean
	public Binding logDLQBinding() {
		return BindingBuilder.bind(logDLQueue()).to(reportingDeadLetterExchange()).with(QUEUE_LOG_DLQ);
	}
}
