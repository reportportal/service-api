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
import com.epam.ta.reportportal.ws.rabbit.AsyncReportingListener;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantin Antipin
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class ReportingConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ReportingConfiguration.class);

  public static final long DEAD_LETTER_DELAY_MILLIS = 60_000L;
  public static final long DEAD_LETTER_MAX_RETRY = 10L;

  /**
   * Exchanges
   */
  public static final String EXCHANGE_REPORTING = "reporting";
  public static final String EXCHANGE_REPORTING_RETRY = "reporting.retry";

  /**
   * Queue definitions
   */
  public static final String QUEUE_PREFIX = "reporting";
  public static final String QUEUE_RETRY_PREFIX = "reporting.retry";
  public static final String QUEUE_DLQ = "reporting.dlq";

  @Value("${rp.amqp.queues}")
  public int queueAmount;

  /**
   * Cluster configuration parameter. Number of queues to be processed by this service-api pod
   * (default effectively infinite) Note: should correlate with number QUEUE_AMOUNT & number of
   * service-api pods being started in cluster
   */
  @Value("${rp.amqp.queuesPerPod:1000000}")
  private int queuesPerPod;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private ConfigurableListableBeanFactory configurableBeanFactory;

  @Bean
  public Exchange reportingExchange(AmqpAdmin amqpAdmin) {
    Exchange exchange = ExchangeBuilder.directExchange(EXCHANGE_REPORTING).durable(true).build();
    amqpAdmin.declareExchange(exchange);
    return exchange;
  }

  @Bean
  public Exchange reportingRetryExchange(AmqpAdmin amqpAdmin) {
    Exchange exchange = ExchangeBuilder.directExchange(EXCHANGE_REPORTING_RETRY).durable(true)
        .build();
    amqpAdmin.declareExchange(exchange);
    return exchange;
  }

  @Bean
  public List<Queue> queues(AmqpAdmin amqpAdmin) {
    List<Queue> queues = new ArrayList();
    for (int i = 0; i < queueAmount; i++) {
      String index = String.valueOf(i);
      String queueName = QUEUE_PREFIX + "." + index;
      Queue queue = QueueBuilder.durable(queueName)
          .withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING_RETRY)
          .withArgument("x-dead-letter-routing-key", index)
          .build();
      queue.setShouldDeclare(true);
      queue.setAdminsThatShouldDeclare(amqpAdmin);
      registerSingleton(queueName, queue);
      amqpAdmin.declareQueue(queue);
      queues.add(queue);
    }
    return queues;
  }

  @Bean
  public List<Queue> retryQueues(AmqpAdmin amqpAdmin) {
    List<Queue> queues = new ArrayList();
    for (int i = 0; i < queueAmount; i++) {
      String index = String.valueOf(i);
      String queueName = QUEUE_RETRY_PREFIX + "." + index;
      Queue retryQueue = QueueBuilder.durable(queueName)
          .withArgument("x-dead-letter-exchange", EXCHANGE_REPORTING)
          .withArgument("x-dead-letter-routing-key", index)
          .withArgument("x-message-ttl", DEAD_LETTER_DELAY_MILLIS)
          .build();
      retryQueue.setShouldDeclare(true);
      retryQueue.setAdminsThatShouldDeclare(amqpAdmin);
      registerSingleton(queueName, retryQueue);
      amqpAdmin.declareQueue(retryQueue);
      queues.add(retryQueue);
    }
    return queues;
  }

  @Bean
  public Queue queueDlq(AmqpAdmin amqpAdmin) {
    Queue queue = QueueBuilder.durable(QUEUE_DLQ).build();
    queue.setShouldDeclare(true);
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    amqpAdmin.declareQueue(queue);
    return queue;
  }

  @Bean
  public List<Binding> bindings(AmqpAdmin amqpAdmin,
      @Qualifier("reportingExchange") Exchange reportingExchange,
      @Qualifier("reportingRetryExchange") Exchange reportingRetryExchange,
      @Qualifier("queues") List<Queue> queues,
      @Qualifier("queueDlq") Queue queueDlq, @Qualifier("retryQueues") List<Queue> retryQueues) {
    List<Binding> bindings = new ArrayList<>();
    int i = 0;
    for (Queue queue : queues) {
      String index = String.valueOf(i);
      Binding queueBinding = BindingBuilder.bind(queue).to(reportingExchange).with(index).noargs();
      bindings.add(queueBinding);
      queueBinding.setShouldDeclare(true);
      queueBinding.setAdminsThatShouldDeclare(amqpAdmin);
      amqpAdmin.declareBinding(queueBinding);
      registerSingleton("queueBinding." + queue.getName(), queueBinding);
      i++;
    }
    i = 0;
    for (Queue retryQueue : retryQueues) {
      String index = String.valueOf(i);
      Binding queueBinding = BindingBuilder.bind(retryQueue).to(reportingRetryExchange).with(index)
          .noargs();
      bindings.add(queueBinding);
      queueBinding.setShouldDeclare(true);
      queueBinding.setAdminsThatShouldDeclare(amqpAdmin);
      amqpAdmin.declareBinding(queueBinding);
      registerSingleton("queueBinding." + retryQueue.getName(), queueBinding);
      i++;
    }
    Binding queueBinding = BindingBuilder.bind(queueDlq).to(reportingRetryExchange).with(QUEUE_DLQ)
        .noargs();
    amqpAdmin.declareBinding(queueBinding);

    return bindings;
  }

  @Bean
  @Qualifier("reportingListenerContainers")
  public List<AbstractMessageListenerContainer> listenerContainers(
      ConnectionFactory connectionFactory,
      ApplicationEventPublisher applicationEventPublisher,
      @Qualifier("queues") List<Queue> queues) {
    List<AbstractMessageListenerContainer> containers = new ArrayList<>();
    int consumersCount = 0;
    while (consumersCount < queuesPerPod) {
      SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(
          connectionFactory);
      containers.add(listenerContainer);
      listenerContainer.setConnectionFactory(connectionFactory);
      listenerContainer.addQueueNames(queues.get(consumersCount).getName());
      listenerContainer.setExclusive(true);
      listenerContainer.setMissingQueuesFatal(false);
      listenerContainer.setApplicationEventPublisher(applicationEventPublisher);
      listenerContainer.setupMessageListener(reportingListener());
      listenerContainer.afterPropertiesSet();
      consumersCount++;
      logger.info("Consumer is created, current consumers count is {}", consumersCount);
    }
    return containers;
  }

  @Bean
  public MessageListener reportingListener() {
    return new AsyncReportingListener();
  }

  private void registerSingleton(String name, Object bean) {
    configurableBeanFactory.registerSingleton(name.trim(), bean);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
    applicationContext.getAutowireCapableBeanFactory().initializeBean(bean, name);
  }

}
