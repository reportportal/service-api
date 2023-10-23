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

package com.epam.ta.reportportal.core.logging;

import static com.epam.ta.reportportal.core.logging.HelperUtil.checkLoggingRecords;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RabbitMessageLoggingAspectTest {

  private static HelperListener proxy;

  private static RabbitMessageLoggingAspect aspect;

  private static Message message;

  private static Map<Object, Object> payload;

  private static Logger logger;

  @Mock
  private static Appender<ILoggingEvent> appender;

  @Mock
  private static RabbitMessageLogging annotation;

  private String log;

  private static ObjectMapper objectMapper;

  private static MessageConverter messageConverter;

  private static Map<String, Object> headers;

  @BeforeAll
  static void beforeAll() throws Exception {
    aspect = new RabbitMessageLoggingAspect();

    objectMapper = new ObjectMapper();
    messageConverter = new Jackson2JsonMessageConverter();

    ReflectionTestUtils.setField(aspect, "objectMapper", objectMapper);
    ReflectionTestUtils.setField(aspect, "messageConverter", messageConverter);

    HelperListener listener = new HelperListener();
    AspectJProxyFactory factory = new AspectJProxyFactory(listener);
    factory.addAspect(aspect);
    proxy = factory.getProxy();

    payload = new HashMap<>();
    payload.put("key1", "one");
    payload.put("key2", "two");
    payload.put("key3", "three");

    headers = new TreeMap<>();
    headers.put("header1", "one");
    headers.put("header2", UUID.randomUUID());
    headers.put("__ContentTypeId__", "java.lang.Object");
    headers.put("__TypeId__", "java.util.HashMap");
    headers.put("__KeyTypeId__", "java.lang.Object");

    MessagePropertiesBuilder properties = MessagePropertiesBuilder.newInstance();
    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      properties.setHeader(entry.getKey(), entry.getValue());
    }
    message = messageConverter.toMessage(payload, properties.build());

    logger = (Logger) LoggerFactory.getLogger(HelperListener.class);
    logger.setLevel(Level.DEBUG);
    logger.setAdditive(false);
  }

  @BeforeEach
  void setup() {
    logger.detachAndStopAllAppenders();
    logger.addAppender(appender);
  }

  @Test
  void testMessageFull() throws Exception {

    when(annotation.logHeaders()).thenReturn(true);
    when(annotation.logBody()).thenReturn(true);

    proxy.onMessageFull(message);
    log = aspect.formatMessageRecord("onMessageFull", headers, payload, annotation);
    System.out.println(log);

    checkLoggingRecords(appender, 1, new Level[]{Level.DEBUG}, log);
  }

  @Test
  void testMessageWithoutHeader() throws Exception {

    when(annotation.logHeaders()).thenReturn(false);
    when(annotation.logBody()).thenReturn(true);

    proxy.onMessageWithoutHeaders(message);
    log = aspect.formatMessageRecord("onMessageWithoutHeaders", headers, payload, annotation);
    System.out.println(log);

    checkLoggingRecords(appender, 1, new Level[]{Level.DEBUG}, log);
  }

  @Test
  void testMessageWithoutBody() throws Exception {

    when(annotation.logHeaders()).thenReturn(true);
    when(annotation.logBody()).thenReturn(false);

    proxy.onMessageWithoutBody(message);
    log = aspect.formatMessageRecord("onMessageWithoutBody", headers, payload, annotation);
    System.out.println(log);

    checkLoggingRecords(appender, 1, new Level[]{Level.DEBUG}, log);
  }

}