/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.reporting.async.config;

import com.epam.ta.reportportal.reporting.async.consumer.ReportingConsumer;
import com.epam.ta.reportportal.reporting.async.exception.ReportingErrorHandler;
import com.epam.ta.reportportal.reporting.async.handler.provider.ReportingHandlerProvider;
import com.rabbitmq.http.client.Client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
@RequiredArgsConstructor
public class ReportingTopologyConfiguration {

  public static final int RETRY_TTL_MILLIS = 10_000;
  public static final String REPORTING_EXCHANGE = "e.reporting";
  public static final String RETRY_EXCHANGE = "e.reporting.retry";
  public static final String DEFAULT_CONSISTENT_HASH_ROUTING_KEY = "";
  public static final String REPORTING_QUEUE_PREFIX = "q.reporting.";
  public static final String RETRY_QUEUE = "q.retry.reporting";
  public static final String TTL_QUEUE = "q.retry.reporting.ttl";
  public static final String REPORTING_PARKING_LOT = "q.parkingLot.reporting";

  private final AmqpAdmin amqpAdmin;
  private final Client managementClient;

  @Value("${reporting.parkingLot.ttl:14}")
  private long parkingLotTtl;

  @Value("${reporting.queues.count:10}")
  private Integer queuesCount;

  @Value("${reporting.consumers.reconnect:true}")
  private Boolean reconnect;

  @Bean
  String instanceUniqueId() {
    String instanceId = UUID.randomUUID().toString();
    return instanceId.substring(instanceId.lastIndexOf("-") + 1);
  }

  @Bean
  Exchange reportingConsistentExchange() {
    Map<String, Object> args = new HashMap<>();
    args.put("hash-header", "hash-on");
    return new CustomExchange(REPORTING_EXCHANGE, "x-consistent-hash", true, false, args);
  }

  @Bean("reportingConsistentQueues")
  List<Queue> reportingConsistentQueues() {
    List<Queue> queues = new ArrayList<>(queuesCount);
    if (reconnect) {
      queues = reconnectToExistedQueues();
    }
    for (int i = queues.size(); i < queuesCount; i++) {
      String queueName = REPORTING_QUEUE_PREFIX + instanceUniqueId() + "." + i;
      Queue queue = buildQueue(queueName);
      queues.add(queue);
    }
    return queues;
  }


  @Bean("reportingConsistentBindings")
  List<Binding> reportingConsistentBindings(
      @Qualifier("reportingConsistentQueues") List<Queue> queues) {
    List<Binding> bindings = new ArrayList<>();
    for (Queue queue : queues) {
      Binding queueBinding = buildQueueBinding(queue);
      amqpAdmin.declareBinding(queueBinding);
      bindings.add(queueBinding);
    }
    return bindings;
  }

  @Bean
  DirectExchange retryExchange() {
    return new DirectExchange(RETRY_EXCHANGE);
  }

  @Bean
  Queue retryQueue() {
    return QueueBuilder.durable(RETRY_QUEUE).deadLetterExchange(RETRY_EXCHANGE)
        .deadLetterRoutingKey(TTL_QUEUE).build();
  }

  @Bean
  Queue ttlQueue() {
    return QueueBuilder.durable(TTL_QUEUE).deadLetterExchange(RETRY_EXCHANGE)
        .deadLetterRoutingKey(RETRY_QUEUE).ttl(RETRY_TTL_MILLIS).build();
  }

  @Bean
  Binding retryQueueBinding() {
    return BindingBuilder.bind(retryQueue()).to(retryExchange()).with(RETRY_QUEUE);
  }

  @Bean
  Binding ttlQueueBinding() {
    return BindingBuilder.bind(ttlQueue()).to(retryExchange()).with(TTL_QUEUE);
  }

  @Bean
  public Queue reportingParkingLot() {
    return QueueBuilder.durable(REPORTING_PARKING_LOT)
        .ttl((int) TimeUnit.DAYS.toMillis(parkingLotTtl))
        .build();
  }


  private Binding buildQueueBinding(Queue queue) {
    String defaultRoutingKey = "1";
    Binding queueBinding = BindingBuilder.bind(queue).to(reportingConsistentExchange())
        .with(defaultRoutingKey).noargs();
    queueBinding.setShouldDeclare(true);
    queueBinding.setAdminsThatShouldDeclare(amqpAdmin);
    return queueBinding;
  }


  private List<Queue> reconnectToExistedQueues() {
    return managementClient.getQueues().stream()
        .filter(q -> q.getName().startsWith(REPORTING_QUEUE_PREFIX))
        .filter(q -> q.getConsumerCount() == 0)
        .map(q -> buildQueue(q.getName()))
        .collect(Collectors.toList());
  }

  private Queue buildQueue(String queueName) {
    Queue queue = QueueBuilder.durable(queueName)
        .deadLetterExchange(RETRY_EXCHANGE)
        .deadLetterRoutingKey(TTL_QUEUE)
        .build();
    queue.setShouldDeclare(true);
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    amqpAdmin.declareQueue(queue);
    return queue;
  }


  @Bean("listenerContainers")
  public List<AbstractMessageListenerContainer> listenerContainers(
      ConnectionFactory connectionFactory,
      ApplicationEventPublisher applicationEventPublisher,
      ReportingHandlerProvider reportingHandlerProvider,
      ReportingErrorHandler errorHandler,
      @Qualifier("reportingConsistentQueues") List<Queue> queues) {
    List<AbstractMessageListenerContainer> containers = new ArrayList<>();
    queues.forEach(q -> {
      SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(
          connectionFactory);
      containers.add(listenerContainer);
      listenerContainer.setConnectionFactory(connectionFactory);
      listenerContainer.addQueueNames(q.getName());
      listenerContainer.setErrorHandler(errorHandler);
      listenerContainer.setExclusive(true);
      listenerContainer.setMissingQueuesFatal(false);
      listenerContainer.setApplicationEventPublisher(applicationEventPublisher);
      listenerContainer.setupMessageListener(reportingListener(reportingHandlerProvider));
      listenerContainer.afterPropertiesSet();
      containers.add(listenerContainer);
    });
    return containers;
  }

  @Bean
  public MessageListener reportingListener(ReportingHandlerProvider reportingHandlerProvider) {
    return new ReportingConsumer(reportingHandlerProvider);
  }
}
