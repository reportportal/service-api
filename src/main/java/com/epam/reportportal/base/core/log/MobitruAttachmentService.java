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

package com.epam.reportportal.base.core.log;

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadProducer;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Shared logic for the Mobitru attribute handlers (launch and test item). Encapsulates the
 * recording key recognition and the creation of a {@code mobitru} system log together with the
 * external attachment load event for a single recording attribute.
 */
@Service
@RequiredArgsConstructor
public class MobitruAttachmentService {

  public static final String MOBILE_RECORDING_ID_KEY = "mobitru_mobile_recording_id";
  public static final String PLAYWRIGHT_RECORDING_ID_KEY = "mobitru_playwright_recording_id";
  public static final String SELENIUM_RECORDING_ID_KEY = "mobitru_selenium_recording_id";
  public static final String PLUGIN_ID = "mobitru";

  private static final String LOAD_ATTACHMENT_COMMAND = "loadExternalAttachment";
  private static final String LOG_TYPE_NAME = "mobitru";
  private static final String LOG_MESSAGE = "Mobitru video. RecordId: %s";
  private static final Set<String> RECORDING_KEYS = Set.of(MOBILE_RECORDING_ID_KEY,
      PLAYWRIGHT_RECORDING_ID_KEY, SELENIUM_RECORDING_ID_KEY);

  private final SystemLogService systemLogService;
  private final ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  /**
   * Extracts the non-empty Mobitru recording attributes from the given attribute set, preserving
   * the original key so it can be forwarded to the plugin.
   *
   * @param attributes Merged attributes of a launch or test item (may be {@code null}/empty)
   * @return Recording attributes to process, never {@code null}
   */
  public List<RecordingAttribute> extractRecordingAttributes(Collection<ItemAttribute> attributes) {
    if (CollectionUtils.isEmpty(attributes)) {
      return List.of();
    }
    return attributes.stream()
        .filter(attr -> StringUtils.hasText(attr.getKey()))
        .filter(attr -> StringUtils.hasText(attr.getValue()))
        .filter(attr -> RECORDING_KEYS.contains(attr.getKey()))
        .map(attr -> new RecordingAttribute(attr.getKey(), attr.getValue()))
        .toList();
  }

  /**
   * Writes a {@code mobitru} system log attached to the launch and publishes the external
   * attachment load event for the given recording.
   */
  public void attachToLaunch(Launch launch, RecordingAttribute attribute) {
    Long logId = systemLogService.writeLaunchLog(launch, LOG_TYPE_NAME,
        String.format(LOG_MESSAGE, attribute.value()));
    externalAttachmentLoadProducer.publish(PLUGIN_ID, LOAD_ATTACHMENT_COMMAND, logId,
        launch.getProjectId(), launch.getId(), null, attribute.value(), attribute.key());
  }

  /**
   * Writes a {@code mobitru} system log attached to the test item and publishes the external
   * attachment load event for the given recording.
   */
  public void attachToTestItem(TestItem testItem, Launch launch, RecordingAttribute attribute) {
    Long logId = systemLogService.writeTestItemLog(testItem, launch, LOG_TYPE_NAME,
        String.format(LOG_MESSAGE, attribute.value()));
    externalAttachmentLoadProducer.publish(PLUGIN_ID, LOAD_ATTACHMENT_COMMAND, logId,
        launch.getProjectId(), launch.getId(), testItem.getItemId(), attribute.value(),
        attribute.key());
  }

  /**
   * A recognized Mobitru recording attribute: its key and non-empty value (the external record
   * id).
   */
  public record RecordingAttribute(String key, String value) {

  }
}
