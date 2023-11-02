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

package com.epam.ta.reportportal.core.analyzer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
public class PatternAnalysisRabbitConfiguration {

  public static final String PATTERN_ANALYSIS_QUEUE = "analysis.pattern.queue";

  @Bean
  public Queue patternAnalysisQueue() {
    return QueueBuilder.durable(PATTERN_ANALYSIS_QUEUE).build();
  }

  @Bean
  public RabbitListenerContainerFactory<SimpleMessageListenerContainer> patternAnalysisContainerFactory(
      ConnectionFactory connectionFactory,
      SimpleRabbitListenerContainerFactoryConfigurer configurer,
      @Value("${rp.environment.variable.pattern-analysis.consumers-count:2}") int consumersCount,
      @Value("${rp.environment.variable.pattern-analysis.prefetch-count:0}") int prefetchCount) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConcurrentConsumers(consumersCount);
    factory.setPrefetchCount(prefetchCount);
    factory.setDefaultRequeueRejected(false);
    configurer.configure(factory, connectionFactory);
    return factory;
  }


}
