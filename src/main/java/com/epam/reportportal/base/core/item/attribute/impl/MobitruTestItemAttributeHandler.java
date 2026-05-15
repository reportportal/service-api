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
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * On test item finish, scans merged attributes for the {@code MBID} key (case-sensitive). For every
 * non-empty value a system log of type {@code mobitru} is created and attached to the test item.
 * No-op when the Mobitru plugin is not currently loaded and enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MobitruTestItemAttributeHandler implements TestItemAttributeHandler {

  public static final String MBID_KEY = "MBID";
  private static final String PLUGIN_ID = "mobitru";
  private static final String LOG_TYPE_NAME = "mobitru";

  private final PluginAvailabilityChecker pluginAvailabilityChecker;
  private final SystemLogService systemLogService;
  private final LaunchRepository launchRepository;
  private final ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  @Override
  public void handleTestItemFinish(TestItem testItem) {
    if (!pluginAvailabilityChecker.isAvailable(PLUGIN_ID)) {
      return;
    }
    if (testItem == null) {
      return;
    }
    Set<ItemAttribute> attributes = testItem.getAttributes();
    if (attributes == null || attributes.isEmpty()) {
      return;
    }
    List<String> recordIds = attributes.stream()
        .filter(attr -> MBID_KEY.equals(attr.getKey()))
        .map(ItemAttribute::getValue)
        .filter(value -> value != null && !value.isEmpty())
        .toList();
    if (recordIds.isEmpty()) {
      return;
    }
    Optional<Launch> launch = Optional.ofNullable(testItem.getLaunchId())
        .flatMap(launchRepository::findById);
    if (launch.isEmpty()) {
      log.warn("Skipping Mobitru log creation for test item {}: launch could not be resolved",
          testItem.getItemId());
      return;
    }
    Launch launchEntity = launch.get();

    recordIds.forEach(value -> {
      Long logId = systemLogService.writeTestItemLog(testItem, launchEntity, LOG_TYPE_NAME, value);
      externalAttachmentLoadProducer.publish(logId, launchEntity.getProjectId(),
          launchEntity.getId(),
          testItem.getItemId(), value);
    });
  }
}
