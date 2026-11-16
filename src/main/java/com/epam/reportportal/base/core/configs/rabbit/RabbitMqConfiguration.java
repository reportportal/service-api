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

import com.epam.reportportal.base.core.configs.Conditions;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.http.client.Client;
import java.net.URI;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Main application RabbitMQ exchanges, queues, and listeners.
 *
 * @author Pavel Bortnik
 */
@EnableRabbit
@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class RabbitMqConfiguration {

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Message converter for publishing messages.
   */
  @Bean
  @Primary
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  /**
   * Message converter for consuming messages. Uses FallbackEventTypeMapper to handle events from external services that
   * may use different package structures.
   */
  @Bean
  public MessageConverter jsonMessageConverterWithFallback(FallbackEventTypeMapper typeMapper) {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
    converter.setJavaTypeMapper(typeMapper);
    return converter;
  }

  @Bean
  public ConnectionFactory connectionFactory(@Value("${rp.amqp.api-address}") String apiAddress,
      @Value("${rp.amqp.addresses}") URI addresses,
      @Value("${rp.amqp.base-vhost}") String virtualHost) {
    try {
      Client client = new Client(apiAddress);
      client.createVhost(virtualHost);
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Unable to create RabbitMq virtual host: " + e.getMessage()
      );
    }
    final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(
        addresses);
    cachingConnectionFactory.setVirtualHost(virtualHost);
    cachingConnectionFactory.setPublisherConfirmType(ConfirmType.SIMPLE);
    return cachingConnectionFactory;
  }

  @Bean
  public AmqpAdmin amqpAdmin(@Autowired ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean(name = "rabbitTemplate")
  public RabbitTemplate rabbitTemplate(
      @Autowired @Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      @Autowired @Qualifier("connectionFactory") ConnectionFactory connectionFactory,
      FallbackEventTypeMapper typeMapper) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setDefaultRequeueRejected(false);
    factory.setErrorHandler(new ConditionalRejectingErrorHandler());
    factory.setAutoStartup(true);
    factory.setMessageConverter(jsonMessageConverterWithFallback(typeMapper));
    return factory;
  }

}
