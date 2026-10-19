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
import com.epam.reportportal.base.util.TestJsonMapper;
import com.rabbitmq.http.client.Client;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration;
import org.springframework.boot.rabbitmq.autoconfigure.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.modulith.Modulithic;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@SpringBootConfiguration
@EnableConfigurationProperties
@Modulithic
@EnableAutoConfiguration(exclude = {QuartzAutoConfiguration.class, RabbitAutoConfiguration.class})
@ComponentScan(value = {"com.epam.reportportal"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.auth.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.base.ws.rabbit.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.reportportal.base.reporting.async.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.epam.reportportal.base.job.*"}),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "com.epam.reportportal.base.core.integration.migration.*"}),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApplicationContextAwareFactoryBeanTest.TestConfig.class)})
public class TestConfig {

  @Value("${rp.jwt.signing-key}")
  private String secret;

  @Autowired
  private DefaultUserDetailsService userDetailsService;

  @Autowired
  private DataSource dataSource;

  @Bean
  @Profile("unittest")
  protected RabbitMqManagementClient managementTemplate(@Qualifier("rabbitClient") Client rabbitClient) {
    return new RabbitMqManagementClientTemplate(rabbitClient, "analyzer");
  }

  @Bean
  @Profile("unittest")
  public Flyway flyway() {
    return Flyway.configure().dataSource(dataSource).validateOnMigrate(false).load();
  }

  @Bean(name = "analyzerRabbitListenerContainerFactory")
  public SimpleRabbitListenerContainerFactory analyzerRabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setAutoStartup(false);
    return factory;
  }

  @Bean
  public JsonMapper testObjectMapper() {
    return TestJsonMapper.INSTANCE;
  }

}
