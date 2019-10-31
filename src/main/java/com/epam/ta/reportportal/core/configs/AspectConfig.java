package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.logging.HttpLoggingAspect;
import com.epam.ta.reportportal.core.logging.RabbitMessageLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantin Antipin
 */
@Configuration
public class AspectConfig {

    @Bean
    @ConditionalOnProperty(name = "rp.requestLogging", havingValue = "true")
    HttpLoggingAspect httpLoggingAspect() {
        return new HttpLoggingAspect();
    }

    @Bean
    @ConditionalOnProperty(name = "rp.requestLogging", havingValue = "true")
    RabbitMessageLoggingAspect rabbitMessageLoggingAspect() {
        return new RabbitMessageLoggingAspect();
    }
}
