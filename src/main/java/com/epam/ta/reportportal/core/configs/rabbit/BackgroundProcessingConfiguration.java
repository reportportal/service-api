package com.epam.ta.reportportal.core.configs.rabbit;

import com.epam.ta.reportportal.core.configs.Conditions;
import org.springframework.amqp.core.*;
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
