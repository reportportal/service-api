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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.MessageBusImpl;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Base64UrlNamingStrategy;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Bortnik
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class InternalConfiguration {

  /**
   * Exchanges
   */
  public static final String EXCHANGE_EVENTS = "broadcast.events";
  public static final String EXCHANGE_ACTIVITY = "activity";
  public static final String EXCHANGE_ATTACHMENT = "attachment";

  /**
   * Queues
   */
  public static final String KEY_EVENTS = "broadcast.events";
  public static final String QUEUE_ACTIVITY = "activity";
  public static final String QUEUE_ACTIVITY_KEY = "activity.#";
  public static final String QUEUE_ATTACHMENT_DELETE = "attachment.delete";

  public static final String QUEUE_QUERY_RQ = "query-rq";

  @Bean
  public MessageBus messageBus(
      @Autowired @Qualifier(value = "rabbitTemplate") AmqpTemplate amqpTemplate) {
    return new MessageBusImpl(amqpTemplate);
  }

  /**
   * Exchanges definition
   */

  @Bean
  public FanoutExchange eventsExchange() {
    return new FanoutExchange(EXCHANGE_EVENTS, false, false);
  }

  @Bean
  public TopicExchange activityExchange() {
    return new TopicExchange(EXCHANGE_ACTIVITY, true, false);
  }

  @Bean
  public DirectExchange attachmentExchange() {
    return new DirectExchange(EXCHANGE_ATTACHMENT, true, false);
  }

  /**
   * Queues definition
   */

  @Bean
  public Queue eventsQueue() {
    return new AnonymousQueue(new Base64UrlNamingStrategy(KEY_EVENTS + "."));
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
  public Queue queryQueue() {
    return new Queue(QUEUE_QUERY_RQ);
  }

  /**
   * Bindings
   */

  @Bean
  public Binding eventsQueueBinding() {
    return BindingBuilder.bind(eventsQueue()).to(eventsExchange());
  }

  @Bean
  public Binding eventsActivityBinding() {
    return BindingBuilder.bind(activityQueue()).to(activityExchange()).with(QUEUE_ACTIVITY_KEY);
  }

  @Bean
  public Binding attachmentDeleteBinding() {
    return BindingBuilder.bind(deleteAttachmentQueue()).to(attachmentExchange())
        .with(QUEUE_ATTACHMENT_DELETE);
  }

}
