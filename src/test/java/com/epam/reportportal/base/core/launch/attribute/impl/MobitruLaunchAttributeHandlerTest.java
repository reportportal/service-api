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

package com.epam.reportportal.base.core.launch.attribute.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadProducer;
import com.epam.reportportal.base.core.log.MobitruAttachmentService;
import com.epam.reportportal.base.core.log.SystemLogService;
import com.epam.reportportal.base.core.plugin.PluginAvailabilityChecker;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MobitruLaunchAttributeHandlerTest {

  private static final String PLUGIN_ID = "mobitru";
  private static final String LOAD_EXTERNAL_ATTACHMENT_COMMAND = "loadExternalAttachment";
  private static final String LOG_TYPE_NAME = "mobitru";
  private static final String LOG_MESSAGE = "Mobitru video. RecordId: %s";
  private static final String MOBILE_RECORDING_ID_KEY = "mobitru_mobile_recording_id";
  private static final String PLAYWRIGHT_RECORDING_ID_KEY = "mobitru_playwright_recording_id";
  private static final String SELENIUM_RECORDING_ID_KEY = "mobitru_selenium_recording_id";

  @Mock
  private PluginAvailabilityChecker pluginAvailabilityChecker;

  @Mock
  private SystemLogService systemLogService;

  @Mock
  private ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  private MobitruLaunchAttributeHandler handler;

  @BeforeEach
  void setUp() {
    MobitruAttachmentService recordingAttachmentService =
        new MobitruAttachmentService(pluginAvailabilityChecker, systemLogService,
            externalAttachmentLoadProducer);
    handler = new MobitruLaunchAttributeHandler(recordingAttachmentService);
  }

  @Test
  void doesNothingWhenPluginNotAvailable() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(false);

    handler.handleLaunchFinish(launchWithAttribute(MOBILE_RECORDING_ID_KEY, "device-1"));

    verifyNoInteractions(systemLogService);
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void writesOneLogPerMobileRecordingAttribute() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    Launch launch = launchWithAttributes(
        attr(MOBILE_RECORDING_ID_KEY, "device-1"),
        attr(MOBILE_RECORDING_ID_KEY, "device-2"),
        attr("otherKey", "x")
    );
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-1"))))
        .thenReturn(11L);
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-2"))))
        .thenReturn(22L);

    handler.handleLaunchFinish(launch);

    verify(systemLogService).writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-1")));
    verify(systemLogService).writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-2")));
    verify(systemLogService, times(2))
        .writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), anyString());
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(11L), eq(2L), eq(1L), isNull(),
        eq("device-1"), eq(MOBILE_RECORDING_ID_KEY));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(22L), eq(2L), eq(1L), isNull(),
        eq("device-2"), eq(MOBILE_RECORDING_ID_KEY));
  }

  @Test
  void writesOneLogPerPlaywrightRecordingAttribute() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    Launch launch = launchWithAttributes(
        attr(PLAYWRIGHT_RECORDING_ID_KEY, "device-1"),
        attr(PLAYWRIGHT_RECORDING_ID_KEY, "device-2"),
        attr("otherKey", "x")
    );
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-1"))))
        .thenReturn(11L);
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-2"))))
        .thenReturn(22L);

    handler.handleLaunchFinish(launch);

    verify(systemLogService).writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-1")));
    verify(systemLogService).writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-2")));
    verify(systemLogService, times(2))
        .writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), anyString());
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(11L), eq(2L), eq(1L), isNull(),
        eq("device-1"), eq(PLAYWRIGHT_RECORDING_ID_KEY));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(22L), eq(2L), eq(1L), isNull(),
        eq("device-2"), eq(PLAYWRIGHT_RECORDING_ID_KEY));
  }

  @Test
  void writesLogForSeleniumRecordingAttribute() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    Launch launch = launchWithAttribute(SELENIUM_RECORDING_ID_KEY, "device-1");
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-1"))))
        .thenReturn(11L);

    handler.handleLaunchFinish(launch);

    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(11L), eq(2L), eq(1L), isNull(),
        eq("device-1"), eq(SELENIUM_RECORDING_ID_KEY));
  }

  @Test
  void writesLogsForMixedMobitruRecordingAttributes() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    Launch launch = launchWithAttributes(
        attr(MOBILE_RECORDING_ID_KEY, "device-mobile"),
        attr(PLAYWRIGHT_RECORDING_ID_KEY, "device-playwright"),
        attr(SELENIUM_RECORDING_ID_KEY, "device-selenium")
    );
    when(
        systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
            eq(logMessage("device-mobile"))))
        .thenReturn(11L);
    when(
        systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
            eq(logMessage("device-playwright"))))
        .thenReturn(22L);
    when(
        systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
            eq(logMessage("device-selenium"))))
        .thenReturn(33L);

    handler.handleLaunchFinish(launch);

    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(11L), eq(2L), eq(1L), isNull(),
        eq("device-mobile"), eq(MOBILE_RECORDING_ID_KEY));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(22L), eq(2L), eq(1L), isNull(),
        eq("device-playwright"), eq(PLAYWRIGHT_RECORDING_ID_KEY));
    verify(externalAttachmentLoadProducer).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(33L), eq(2L), eq(1L), isNull(),
        eq("device-selenium"), eq(SELENIUM_RECORDING_ID_KEY));
  }

  @Test
  void doesNothingWhenNoMbidAttribute() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);

    handler.handleLaunchFinish(launchWithAttribute("retentionPolicy", "important"));

    verify(systemLogService, never()).writeLaunchLog(any(), anyString(), anyString());
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  @Test
  void skipsEmptyAndNullValues() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);
    Launch launch = launchWithAttributes(
        attr(MOBILE_RECORDING_ID_KEY, ""),
        attr(MOBILE_RECORDING_ID_KEY, null),
        attr(MOBILE_RECORDING_ID_KEY, "device-x")
    );
    when(systemLogService.writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME), eq(logMessage("device-x"))))
        .thenReturn(99L);

    handler.handleLaunchFinish(launch);

    verify(systemLogService).writeLaunchLog(eq(launch), eq(LOG_TYPE_NAME),
        eq(logMessage("device-x")));
    verify(systemLogService, times(1)).writeLaunchLog(any(), anyString(), anyString());
    verify(externalAttachmentLoadProducer, times(1)).publish(eq(PLUGIN_ID),
        eq(LOAD_EXTERNAL_ATTACHMENT_COMMAND), eq(99L), eq(2L), eq(1L), isNull(),
        eq("device-x"), eq(MOBILE_RECORDING_ID_KEY));
  }

  @Test
  void isCaseSensitiveOnKey() {
    when(pluginAvailabilityChecker.isAvailable(PLUGIN_ID)).thenReturn(true);

    handler.handleLaunchFinish(launchWithAttribute("mbid", "device-1"));

    verify(systemLogService, never()).writeLaunchLog(any(), anyString(), anyString());
    verifyNoInteractions(externalAttachmentLoadProducer);
  }

  private Launch launchWithAttribute(String key, String value) {
    return launchWithAttributes(attr(key, value));
  }

  private Launch launchWithAttributes(ItemAttribute... attrs) {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setProjectId(2L);
    Set<ItemAttribute> set = new LinkedHashSet<>();
    int idCounter = 1;
    for (ItemAttribute a : attrs) {
      a.setId((long) idCounter++);
      set.add(a);
    }
    launch.setAttributes(set);
    return launch;
  }

  private ItemAttribute attr(String key, String value) {
    return new ItemAttribute(key, value, false);
  }

  private String logMessage(String value) {
    return String.format(LOG_MESSAGE, value);
  }
}
