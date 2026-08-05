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

package com.epam.reportportal.base.core.configs.rabbit;

import com.epam.reportportal.base.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.reportportal.base.core.analyzer.auto.client.impl.RabbitMqManagementClientTemplate;
import com.epam.reportportal.base.core.configs.Conditions;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.rabbitmq.http.client.Client;
import java.net.URI;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ objects used for analyzer service messaging.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class AnalyzerRabbitMqConfiguration {

  @Autowired
  private MessageConverter messageConverter;

  @Bean
  public RabbitMqManagementClient managementTemplate(
      @Value("${rp.amqp.api-address}") String address,
      @Value("${rp.amqp.analyzer-vhost}") String virtualHost) {
    Client rabbitClient;
    try {
      rabbitClient = new Client(address);
    } catch (Exception e) {
      throw new ReportPortalException(
          ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Cannot create a HTTP rabbit client instance. Incorrect api address " + address
      );
    }
    return new RabbitMqManagementClientTemplate(rabbitClient, virtualHost);
  }

  @Bean(name = "analyzerConnectionFactory")
  public ConnectionFactory analyzerConnectionFactory(@Value("${rp.amqp.addresses}") URI addresses,
      @Value("${rp.amqp.analyzer-vhost}") String virtualHost) {
    CachingConnectionFactory factory = new CachingConnectionFactory(addresses);
    factory.setVirtualHost(virtualHost);
    return factory;
  }

  @Bean(name = "analyzerRabbitTemplate")
  public RabbitTemplate analyzerRabbitTemplate(
      @Autowired @Qualifier("analyzerConnectionFactory") ConnectionFactory connectionFactory,
      @Value("${rp.amqp.reply-timeout}") long replyTimeout) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);
    rabbitTemplate.setReplyTimeout(replyTimeout);
    return rabbitTemplate;
  }

  @Bean(name = "analyzerRabbitAdmin")
  public RabbitAdmin analyzerRabbitAdmin(
      @Autowired @Qualifier("analyzerConnectionFactory") ConnectionFactory connectionFactory) {
    RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
    // declare only declarable that explicitly reference this admin, so it does not
    // attempt to create base-vhost queues in the analyzer vhost
    rabbitAdmin.setExplicitDeclarationsOnly(true);
    return rabbitAdmin;
  }

  @Bean(name = "analyzerReplyQueue")
  public Queue analyzerReplyQueue(
      @Value("${rp.amqp.analyzer-reply-queue:analysis-reply}") String replyQueueName,
      @Autowired @Qualifier("analyzerRabbitAdmin") RabbitAdmin analyzerRabbitAdmin) {
    Queue queue = new Queue(replyQueueName, true);
    queue.setAdminsThatShouldDeclare(analyzerRabbitAdmin);
    return queue;
  }

  @Bean(name = "analyzerRabbitListenerContainerFactory")
  public SimpleRabbitListenerContainerFactory analyzerRabbitListenerContainerFactory(
      @Autowired @Qualifier("analyzerConnectionFactory") ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setDefaultRequeueRejected(false);
    factory.setErrorHandler(new ConditionalRejectingErrorHandler());
    factory.setAutoStartup(true);
    factory.setMessageConverter(messageConverter);
    return factory;
  }

}
