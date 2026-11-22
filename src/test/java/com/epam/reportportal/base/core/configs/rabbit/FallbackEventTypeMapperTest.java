/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.core.plugin.PluginStateChangedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

class FallbackEventTypeMapperTest {

  private final FallbackEventTypeMapper mapper = new FallbackEventTypeMapper();

  @BeforeEach
  void setUp() {
    mapper.init();
  }

  private MessageProperties typeId(String typeId) {
    var properties = new MessageProperties();
    properties.setHeader("__TypeId__", typeId);
    return properties;
  }

  @Test
  void rejectsClassesOutsideTrustedPackage() {
    assertThrows(MessageConversionException.class, () -> mapper.toClass(typeId("java.net.URL")));
  }

  @Test
  void resolvesRegisteredEventByFullName() {
    assertEquals(AssignUserEvent.class, mapper.toClass(typeId(AssignUserEvent.class.getName())));
  }

  /**
   * The same converter serves consumers of non-event payloads (plugin broadcasts, email notifications, external
   * attachment loads, queries), so classes of the trusted package must resolve even when they are not events.
   */
  @Test
  void resolvesNonEventPayloadFromTrustedPackage() {
    assertEquals(PluginStateChangedMessage.class,
        mapper.toClass(typeId(PluginStateChangedMessage.class.getName())));
  }

  @Test
  void resolvesForeignPackageEventBySimpleName() {
    assertEquals(AssignUserEvent.class, mapper.toClass(typeId("com.other.AssignUserEvent")));
  }
}
