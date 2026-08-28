/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.reporting.async.config;

import com.epam.reportportal.base.reporting.async.consumer.ReportingConsumer;
import com.epam.reportportal.base.reporting.async.exception.ReportingErrorHandler;
import com.epam.reportportal.base.reporting.async.handler.provider.ReportingHandlerProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Spring configuration that declares the RabbitMQ exchanges, queues, and bindings for the async reporting topology.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportingTopologyConfiguration {

  public static final String REPORTING_EXCHANGE = "e.reporting";
  public static final String RETRY_EXCHANGE = "e.reporting.retry";
  public static final String DEFAULT_CONSISTENT_HASH_ROUTING_KEY = "";
  public static final String DEFAULT_QUEUE_ROUTING_KEY = "1";
  public static final String REPORTING_QUEUE_PREFIX = "q.reporting.";
  public static final String TTL_QUEUE_MS = "q.retry.reporting.ttl.ms";
  public static final String TTL_QUEUE_S = "q.retry.reporting.ttl.s";
  public static final String TTL_QUEUE_M = "q.retry.reporting.ttl.m";
  public static final String REPORTING_PARKING_LOT = "q.parkingLot.reporting";

  private final AmqpAdmin amqpAdmin;

  private final ApplicationContext applicationContext;

  @Value("${reporting.parkingLot.ttl.days:7}")
  private long parkingLotTtl;

  @Value("${reporting.queues.count:10}")
  private Integer queuesCount;

  @Value("${reporting.consumer.prefetchCount:10}")
  private Integer prefetchCount;

  @Value("${reporting.shutdown.consumer-timeout:PT5S}")
  private Duration consumerShutdownTimeout;

  @Bean
  Exchange reportingConsistentExchange() {
    Map<String, Object> args = new HashMap<>();
    args.put("hash-header", "hash-on");
    return new CustomExchange(REPORTING_EXCHANGE, "x-consistent-hash", true, false, args);
  }

  @Bean("reportingQueues")
  List<Queue> reportingQueues() {
    String instanceId = resolveInstanceId();
    List<Queue> queues = new ArrayList<>(queuesCount);
    for (int i = 0; i < queuesCount; i++) {
      queues.add(declareQueue(REPORTING_QUEUE_PREFIX + instanceId + "." + i));
    }
    log.info("Configured {} reporting queues for instance '{}'", queues.size(), instanceId);
    return queues;
  }

  @Bean("reportingBindings")
  List<Binding> reportingBindings(@Qualifier("reportingQueues") List<Queue> queues) {
    return queues.stream().map(this::declareBinding).toList();
  }

  /**
   * Exposes the per-instance queues and bindings to the {@link AmqpAdmin} so that they are
   * recreated whenever a connection to the broker is (re)established. Without it a broker that lost
   * its state leaves this instance with running consumers and no queues to consume from.
   */
  @Bean
  Declarables reportingDeclarables(@Qualifier("reportingQueues") List<Queue> queues,
      @Qualifier("reportingBindings") List<Binding> bindings) {
    List<Declarable> declarables = new ArrayList<>(queues);
    declarables.addAll(bindings);
    return new Declarables(declarables);
  }

  @Bean
  DirectExchange retryExchange() {
    return new DirectExchange(RETRY_EXCHANGE);
  }

  @Bean
    //0.5s
  Queue ttlQueueMs() {
    return QueueBuilder.durable(TTL_QUEUE_MS).ttl(500).deadLetterExchange(REPORTING_EXCHANGE)
        .deadLetterRoutingKey(DEFAULT_CONSISTENT_HASH_ROUTING_KEY)
        .build();
  }

  @Bean
  Binding ttlQueueMsBinding() {
    return BindingBuilder.bind(ttlQueueMs()).to(retryExchange()).with(TTL_QUEUE_MS);
  }

  @Bean
    //5s
  Queue ttlQueueS() {
    return QueueBuilder.durable(TTL_QUEUE_S).ttl(5000).deadLetterExchange(REPORTING_EXCHANGE)
        .deadLetterRoutingKey(DEFAULT_CONSISTENT_HASH_ROUTING_KEY)
        .build();
  }

  @Bean
  Binding ttlQueueSBinding() {
    return BindingBuilder.bind(ttlQueueS()).to(retryExchange()).with(TTL_QUEUE_S);
  }

  @Bean
    //2m
  Queue ttlQueueM() {
    return QueueBuilder.durable(TTL_QUEUE_M).ttl(120000).deadLetterExchange(REPORTING_EXCHANGE)
        .deadLetterRoutingKey(DEFAULT_CONSISTENT_HASH_ROUTING_KEY)
        .build();
  }

  @Bean
  Binding ttlQueueMBinding() {
    return BindingBuilder.bind(ttlQueueM()).to(retryExchange()).with(TTL_QUEUE_M);
  }

  @Bean
  public Queue reportingParkingLot() {
    return QueueBuilder.durable(REPORTING_PARKING_LOT)
        .ttl((int) TimeUnit.DAYS.toMillis(parkingLotTtl))
        .build();
  }

  /**
   * Resolves the identifier embedded into the names of the queues owned by this instance. A
   * hostname keeps queue ownership recognizable, while a random suffix prevents collisions between
   * deployments that use the same hostnames and RabbitMQ vhost.
   */
  private static String resolveInstanceId() {
    String uuid = UUID.randomUUID().toString();
    String uniqueSuffix = uuid.substring(uuid.lastIndexOf('-') + 1);
    String hostname = System.getenv("HOSTNAME");
    if (StringUtils.hasText(hostname)) {
      return sanitizeInstanceId(hostname) + "." + uniqueSuffix;
    }
    return uniqueSuffix;
  }

  private static String sanitizeInstanceId(String instanceId) {
    return instanceId.trim().replaceAll("[^a-zA-Z0-9._-]", "-");
  }

  private Binding declareBinding(Queue queue) {
    Binding queueBinding = BindingBuilder.bind(queue).to(reportingConsistentExchange())
        .with(DEFAULT_QUEUE_ROUTING_KEY).noargs();
    queueBinding.setAdminsThatShouldDeclare(amqpAdmin);
    return queueBinding;
  }

  private Queue declareQueue(String queueName) {
    Queue queue = QueueBuilder.durable(queueName).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  /**
   * Creates one single-threaded exclusive consumer per reporting queue. The containers are started and stopped by
   * {@link com.epam.reportportal.base.reporting.async.ReportingListenersLifecycle} rather than here, so that consuming
   * begins only after the application context is fully refreshed.
   */
  @Bean("listenerContainers")
  public List<AbstractMessageListenerContainer> listenerContainers(
      ConnectionFactory connectionFactory,
      ApplicationEventPublisher applicationEventPublisher,
      ReportingHandlerProvider reportingHandlerProvider,
      ReportingErrorHandler errorHandler,
      @Qualifier("reportingQueues") List<Queue> queues) {
    MessageListener messageListener = reportingListener(reportingHandlerProvider);
    List<AbstractMessageListenerContainer> containers = new ArrayList<>(queues.size());
    queues.forEach(q -> {
      SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(
          connectionFactory);
      listenerContainer.setBeanName(q.getName());
      listenerContainer.setApplicationContext(applicationContext);
      listenerContainer.setAmqpAdmin(amqpAdmin);
      listenerContainer.addQueueNames(q.getName());
      listenerContainer.setErrorHandler(errorHandler);
      listenerContainer.setExclusive(true);
      listenerContainer.setPrefetchCount(prefetchCount);
      listenerContainer.setDefaultRequeueRejected(false);
      listenerContainer.setMissingQueuesFatal(false);
      listenerContainer.setShutdownTimeout(consumerShutdownTimeout.toMillis());
      listenerContainer.setApplicationEventPublisher(applicationEventPublisher);
      listenerContainer.setupMessageListener(messageListener);
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
