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

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadProducer;
import com.epam.reportportal.base.core.item.attribute.TestItemAttributeHandler;
import com.epam.reportportal.base.core.log.SystemLogService;
import com.epam.reportportal.base.core.plugin.PluginAvailabilityChecker;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * On test item finish, scans merged attributes for Mobitru recording keys (case-sensitive). For
 * every non-empty value a system log of type {@code mobitru} is created and attached to the test
 * item. No-op when the Mobitru plugin is not currently loaded and enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MobitruTestItemAttributeHandler implements TestItemAttributeHandler {

  private static final String MBID_KEY = "MBID";
  private static final String BBID_KEY = "BBID";
  private static final String PLUGIN_ID = "mobitru";
  private static final String LOAD_ATTACHMENT_COMMAND = "loadExternalAttachment";
  private static final String LOG_TYPE_NAME = "mobitru";
  private static final String LOG_MESSAGE = "Mobitru video. RecordId: %s";
  private static final Set<String> RECORDING_KEYS = Set.of(MBID_KEY, BBID_KEY);

  private final PluginAvailabilityChecker pluginAvailabilityChecker;
  private final SystemLogService systemLogService;
  private final LaunchRepository launchRepository;
  private final ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  @Override
  public void handleTestItemFinish(TestItem testItem) {
    if (!validateState(testItem)) {
      return;
    }

    var attributes = testItem.getAttributes();
    var recordAttributes = attributes.stream()
        .filter(attr -> RECORDING_KEYS.contains(attr.getKey()))
        .filter(attr -> StringUtils.hasText(attr.getValue()))
        .map(attr -> new RecordingAttribute(attr.getKey(), attr.getValue()))
        .toList();
    if (recordAttributes.isEmpty()) {
      return;
    }

    var launch = Optional.ofNullable(testItem.getLaunchId())
        .flatMap(launchRepository::findById);
    if (launch.isEmpty()) {
      log.warn("Skipping Mobitru log creation for test item {}: launch could not be resolved",
          testItem.getItemId());
      return;
    }

    var launchEntity = launch.get();
    recordAttributes.forEach(attribute -> {
      var logId = systemLogService.writeTestItemLog(testItem, launchEntity, LOG_TYPE_NAME,
          String.format(LOG_MESSAGE, attribute.value()));
      externalAttachmentLoadProducer.publish(PLUGIN_ID, LOAD_ATTACHMENT_COMMAND, logId,
          launchEntity.getProjectId(), launchEntity.getId(), testItem.getItemId(),
          attribute.value(), attribute.key());
    });
  }

  private boolean validateState(TestItem testItem) {
    if (!pluginAvailabilityChecker.isAvailable(PLUGIN_ID) || testItem == null) {
      return false;
    }
    return !CollectionUtils.isEmpty(testItem.getAttributes());
  }

  private record RecordingAttribute(String key, String value) {

  }
}
