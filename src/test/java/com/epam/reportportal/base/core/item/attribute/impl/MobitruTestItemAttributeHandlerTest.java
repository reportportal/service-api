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

package com.epam.reportportal.base.core.item.attribute.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadProducer;
import com.epam.reportportal.base.core.log.SystemLogService;
import com.epam.reportportal.base.core.plugin.PluginAvailabilityChecker;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MobitruTestItemAttributeHandlerTest {

  private static final String PLUGIN_ID = "mobitru";
  private static final String LOG_TYPE_NAME = "mobitru";

  @Mock
  private PluginAvailabilityChecker pluginAvailabilityChecker;

  @Mock
  private SystemLogService systemLogService;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  @InjectMocks
  private MobitruTestItemAttributeHandler handler;

  @Test
  void doesNothingWhenPluginNotAvailable() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(false);

    handler.handleTestItemFinish(itemWithAttribute("MBID", "device-1"));

    verifyNoInteractions(systemLogService);
    verifyNoInteractions(launchRepository);
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void doesNothingWhenNoMbidAttribute() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);

    handler.handleTestItemFinish(itemWithAttribute("otherKey", "v"));

    verifyNoInteractions(systemLogService);
    verifyNoInteractions(launchRepository);
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void writesOneLogPerNonEmptyMbid() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    TestItem item = itemWithAttributes(
        attr("MBID", "device-1"),
        attr("MBID", "device-2"),
        attr("MBID", ""),
        attr("MBID", null)
    );
    Launch launch = new Launch();
    launch.setId(99L);
    launch.setProjectId(7L);
    when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.of(launch));
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq("device-1"))).thenReturn(11L);
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq("device-2"))).thenReturn(22L);

    handler.handleTestItemFinish(item);

    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq("device-1"));
    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq("device-2"));
    verify(systemLogService, times(2))
        .writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME), anyString());
    verify(externalAttachmentLoadProducer).publish(eq(11L), eq(7L), eq(99L), eq(10L),
        eq("device-1"));
    verify(externalAttachmentLoadProducer).publish(eq(22L), eq(7L), eq(99L), eq(10L),
        eq("device-2"));
  }

  @Test
  void isCaseSensitiveOnKey() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);

    handler.handleTestItemFinish(itemWithAttribute("mbid", "device-1"));

    verify(systemLogService, never())
        .writeTestItemLog(any(), any(), anyString(), anyString());
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void skipsWhenLaunchCannotBeResolved() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    TestItem item = itemWithAttribute("MBID", "device-1");
    when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.empty());

    handler.handleTestItemFinish(item);

    verify(systemLogService, never())
        .writeTestItemLog(any(), any(), anyString(), anyString());
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  private TestItem itemWithAttribute(String key, String value) {
    return itemWithAttributes(attr(key, value));
  }

  private TestItem itemWithAttributes(ItemAttribute... attrs) {
    TestItem item = new TestItem();
    item.setItemId(10L);
    item.setLaunchId(99L);
    Set<ItemAttribute> set = new LinkedHashSet<>();
    int idCounter = 1;
    for (ItemAttribute a : attrs) {
      a.setId((long) idCounter++);
      set.add(a);
    }
    item.setAttributes(set);
    return item;
  }

  private ItemAttribute attr(String key, String value) {
    return new ItemAttribute(key, value, false);
  }
}
