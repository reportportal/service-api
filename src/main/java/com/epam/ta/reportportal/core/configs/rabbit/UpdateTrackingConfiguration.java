package com.epam.ta.reportportal.core.configs.rabbit;

import com.epam.ta.reportportal.core.configs.Conditions;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnProperty(value = "plugins.test-executions.enabled", havingValue = "true")
@Conditional(Conditions.NotTestCondition.class)
public class UpdateTrackingConfiguration {

  public static final String EXCHANGE_UPDATE_TRACKING = "update.tracking";
  public static final String LAUNCH_MODIFIED_QUEUE = "launch.modified";
  public static final String LAUNCH_MODIFIED_ROUTING_KEY = "launch.modified";

  @Bean
  DirectExchange updateTrackingExchange() {
    return new DirectExchange(EXCHANGE_UPDATE_TRACKING, true, false);
  }

  @Bean
  Queue launchModifiedQueue() {
    return new Queue(LAUNCH_MODIFIED_QUEUE, true);
  }

  @Bean
  Binding launchModifiedBinding() {
    return BindingBuilder.bind(launchModifiedQueue())
        .to(updateTrackingExchange())
        .with(LAUNCH_MODIFIED_ROUTING_KEY);
  }

  @Bean
  SimpleRabbitListenerContainerFactory exclusiveLastModifiedContainerFactory(
      @Qualifier("connectionFactory") ConnectionFactory connectionFactory,
      MessageConverter jsonMessageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter);
    factory.setPrefetchCount(1);
    factory.setContainerCustomizer(container -> container.setExclusive(true));
    factory.setDefaultRequeueRejected(false);
    factory.setErrorHandler(new ConditionalRejectingErrorHandler());
    return factory;
  }
}
