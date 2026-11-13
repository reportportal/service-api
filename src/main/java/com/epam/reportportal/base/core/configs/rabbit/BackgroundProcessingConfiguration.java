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
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(Conditions.NotTestCondition.class)
public class BackgroundProcessingConfiguration {

  public static final String LOG_MESSAGE_SAVING_QUEUE_NAME = "log_message_saving";
  public static final String LOG_MESSAGE_SAVING_ROUTING_KEY = "log_message_saving";
  public static final String PROCESSING_EXCHANGE_NAME = "processing";

  @Bean
  Queue logMessageSavingQueue() {
    return new Queue(LOG_MESSAGE_SAVING_QUEUE_NAME);
  }

  @Bean
  DirectExchange exchangeProcessing() {
    return new DirectExchange(PROCESSING_EXCHANGE_NAME);
  }

  @Bean
  Binding bindingSavingLogs(@Qualifier("logMessageSavingQueue") Queue queue,
      @Qualifier("exchangeProcessing") DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(LOG_MESSAGE_SAVING_ROUTING_KEY);
  }
}
