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

package com.epam.reportportal.base;

import com.epam.reportportal.base.auth.userdetails.DefaultUserDetailsService;
import com.epam.reportportal.base.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.reportportal.base.core.analyzer.auto.client.impl.RabbitMqManagementClientTemplate;
import com.epam.reportportal.base.util.ApplicationContextAwareFactoryBeanTest;
import com.epam.reportportal.base.ws.resolver.JacksonViewAwareModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
@EnableAutoConfiguration(exclude = {QuartzAutoConfiguration.class, RabbitAutoConfiguration.class})
@ComponentScan(value = {"com.epam.reportportal.base"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.auth.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.base.ws.rabbit.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.base.reporting.async.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.epam.reportportal.base.job.*"}),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.epam.reportportal.base.core.integration.migration.*"}),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApplicationContextAwareFactoryBeanTest.TestConfig.class)})
public class TestConfig {

  @MockBean
  protected Client rabbitClient;

  @MockBean(name = "analyzerRabbitTemplate")
  protected RabbitTemplate analyzerRabbitTemplate;

  @MockBean(name = "rabbitTemplate")
  protected RabbitTemplate rabbitTemplate;

  @MockBean(name = "connectionFactory")
  protected ConnectionFactory connectionFactory;

  @MockBean(name = "simpleRabbitListenerContainerFactoryConfigurer")
  protected SimpleRabbitListenerContainerFactoryConfigurer simpleRabbitListenerContainerFactoryConfigurer;

  @MockBean(name = "amqpAdmin")
  protected AmqpAdmin amqpAdmin;

  @MockBean
  protected MessageConverter messageConverter;

  @Autowired
  private DefaultUserDetailsService userDetailsService;

  @Bean
  @Profile("unittest")
  protected RabbitMqManagementClient managementTemplate() {
    return new RabbitMqManagementClientTemplate(rabbitClient, "analyzer");
  }

  @Bean
  public ObjectMapper testObjectMapper() {
    ObjectMapper om = JsonMapper.builder()
        .annotationIntrospector(new JacksonAnnotationIntrospector())
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .build();

    om.registerModule(new JacksonViewAwareModule(om));
    om.registerModule(new JavaTimeModule());

    return om;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
