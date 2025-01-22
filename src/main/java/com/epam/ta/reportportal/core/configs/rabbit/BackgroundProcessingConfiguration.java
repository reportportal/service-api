package com.epam.ta.reportportal.core.configs.rabbit;

import com.epam.ta.reportportal.core.configs.Conditions;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "rp.searchengine", name = "host")
@Conditional(Conditions.NotTestCondition.class)
public class BackgroundProcessingConfiguration {

  public static final String LOG_MESSAGE_SAVING_QUEUE_NAME = "log_message_saving";
  public static final String LOG_MESSAGE_SAVING_ROUTING_KEY = "log_message_saving";
  public static final String PROCESSING_EXCHANGE_NAME = "processing";

  @Bean
  Queue logMessageSavingQueue() {
    return QueueBuilder.durable(LOG_MESSAGE_SAVING_QUEUE_NAME).quorum().build();
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
