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
import com.epam.reportportal.base.core.log.MobitruAttachmentService;
import com.epam.reportportal.base.core.log.SystemLogService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MobitruTestItemAttributeHandlerTest {

  private static final String PLUGIN_ID = "mobitru";
  private static final String LOAD_EXTERNAL_ATTACHMENT_COMMAND = "loadExternalAttachment";
  private static final String LOG_TYPE_NAME = "mobitru";
  private static final String LOG_MESSAGE = "Mobitru video. RecordId: %s";
  private static final String MOBILE_RECORDING_ID_KEY = "mobitru_mobile_recording_id";
  private static final String PLAYWRIGHT_RECORDING_ID_KEY = "mobitru_playwright_recording_id";
  private static final String SELENIUM_RECORDING_ID_KEY = "mobitru_selenium_recording_id";

  @Mock
  private SystemLogService systemLogService;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  private MobitruTestItemAttributeHandler handler;

  @BeforeEach
  void setUp() {
    MobitruAttachmentService recordingAttachmentService =
        new MobitruAttachmentService(systemLogService, externalAttachmentLoadProducer);
    handler = new MobitruTestItemAttributeHandler(recordingAttachmentService, launchRepository);
  }

  @Test
  void doesNothingWhenNoMobitruRecordingAttribute() {
    handler.handleTestItemFinish(itemWithAttribute("otherKey", "v"));

    verifyNoInteractions(systemLogService);
    verifyNoInteractions(launchRepository);
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void writesOneLogPerNonEmptyMobileRecordingId() {
    TestItem item = itemWithAttributes(
        attr(MOBILE_RECORDING_ID_KEY, "device-1"),
        attr(MOBILE_RECORDING_ID_KEY, "device-2"),
        attr(MOBILE_RECORDING_ID_KEY, ""),
        attr(MOBILE_RECORDING_ID_KEY, null)
    );
    Launch launch = new Launch();
    launch.setId(99L);
    launch.setProjectId(7L);
    when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.of(launch));
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-1")))).thenReturn(11L);
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-2")))).thenReturn(22L);

    handler.handleTestItemFinish(item);

    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-1")));
    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-2")));
    verify(systemLogService, times(2))
        .writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME), anyString());
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(11L), eq(7L), eq(99L), eq(10L),
        eq("device-1"), eq(MOBILE_RECORDING_ID_KEY));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(22L), eq(7L), eq(99L), eq(10L),
        eq("device-2"), eq(MOBILE_RECORDING_ID_KEY));
  }

  @Test
  void writesLogAndPublishesAttributeKeyForPlaywrightRecordingId() {
    TestItem item = itemWithAttribute(PLAYWRIGHT_RECORDING_ID_KEY, "browser-session-1");
    Launch launch = new Launch();
    launch.setId(99L);
    launch.setProjectId(7L);
    when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.of(launch));
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("browser-session-1")))).thenReturn(33L);

    handler.handleTestItemFinish(item);

    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("browser-session-1")));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(33L), eq(7L), eq(99L), eq(10L),
        eq("browser-session-1"), eq(PLAYWRIGHT_RECORDING_ID_KEY));
  }

  @Test
  void writesLogAndPublishesAttributeKeyForSeleniumRecordingId() {
    TestItem item = itemWithAttribute(SELENIUM_RECORDING_ID_KEY, "browser-session-2");
    Launch launch = new Launch();
    launch.setId(99L);
    launch.setProjectId(7L);
    when(launchRepository.findById(item.getLaunchId())).thenReturn(Optional.of(launch));
    when(systemLogService.writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("browser-session-2")))).thenReturn(44L);

    handler.handleTestItemFinish(item);

    verify(systemLogService).writeTestItemLog(eq(item), eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("browser-session-2")));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(44L), eq(7L), eq(99L), eq(10L),
        eq("browser-session-2"), eq(SELENIUM_RECORDING_ID_KEY));
  }

  @Test
  void isCaseSensitiveOnKey() {
    handler.handleTestItemFinish(itemWithAttribute("mbid", "device-1"));

    verify(systemLogService, never())
        .writeTestItemLog(any(), any(), anyString(), anyString());
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void skipsWhenLaunchCannotBeResolved() {
    TestItem item = itemWithAttribute(MOBILE_RECORDING_ID_KEY, "device-1");
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

  private String logMessage(String value) {
    return String.format(LOG_MESSAGE, value);
  }
}
