/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.core.configs.security.JwtReportPortalUserConverter;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.core.analyzer.auto.client.RabbitMqManagementClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.impl.RabbitMqManagementClientTemplate;
import com.epam.ta.reportportal.util.ApplicationContextAwareFactoryBeanTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.http.client.Client;
import io.jsonwebtoken.Jwts.SIG;
import javax.crypto.SecretKey;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
@EnableAutoConfiguration(exclude = {QuartzAutoConfiguration.class, RabbitAutoConfiguration.class})
@ComponentScan(value = {"com.epam.ta.reportportal"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.ws.rabbit.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.reporting.async.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.epam.ta.reportportal.job.*"}),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "com.epam.ta.reportportal.core.integration.migration.*"}),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApplicationContextAwareFactoryBeanTest.TestConfig.class)})
public class TestConfig {

  public final static SecretKey TEST_SECRET = SIG.HS256.key().build();

  @MockitoBean
  protected Client rabbitClient;

  @MockitoBean(name = "analyzerRabbitTemplate")
  protected RabbitTemplate analyzerRabbitTemplate;

  @MockitoBean(name = "rabbitTemplate")
  protected RabbitTemplate rabbitTemplate;

  @MockitoBean(name = "connectionFactory")
  protected ConnectionFactory connectionFactory;

  @MockitoBean(name = "simpleRabbitListenerContainerFactoryConfigurer")
  protected SimpleRabbitListenerContainerFactoryConfigurer simpleRabbitListenerContainerFactoryConfigurer;

  @MockitoBean(name = "amqpAdmin")
  protected AmqpAdmin amqpAdmin;

  @MockitoBean
  protected MessageConverter messageConverter;

  @Autowired
  private DatabaseUserDetailsService userDetailsService;

  @Bean
  @Profile("unittest")
  protected RabbitMqManagementClient managementTemplate() {
    return new RabbitMqManagementClientTemplate(rabbitClient, "analyzer");
  }

  @Bean
  @Profile("unittest")
  public JwtReportPortalUserConverter accessTokenConverter() {
    JwtReportPortalUserConverter jwtConverter = new JwtReportPortalUserConverter(
        userDetailsService);
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
    //jwtGrantedAuthoritiesConverter.setAuthoritiesClaimDelimiter(" ");

    jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtConverter;
  }

  @Bean
  @Profile("unittest")
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(TEST_SECRET).build();
  }

  @Bean
  public ObjectMapper testObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    objectMapper.registerModule(new JavaTimeModule());

    return objectMapper;
  }
}
