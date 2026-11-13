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

import com.epam.reportportal.base.core.item.attribute.TestItemAttributeHandler;
import com.epam.reportportal.base.core.log.MobitruAttachmentService;
import com.epam.reportportal.base.core.log.MobitruAttachmentService.RecordingAttribute;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * On test item finish, scans merged attributes for Mobitru recording keys (case-sensitive). For
 * every non-empty value a system log of type {@code mobitru} is created and attached to the test
 * item.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MobitruTestItemAttributeHandler implements TestItemAttributeHandler {

  private final MobitruAttachmentService mobitruAttachmentService;
  private final LaunchRepository launchRepository;

  @Override
  public void handleTestItemFinish(TestItem testItem) {
    if (testItem == null) {
      return;
    }

    List<RecordingAttribute> recordAttributes =
        mobitruAttachmentService.extractRecordingAttributes(testItem.getAttributes());
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
    recordAttributes.forEach(attribute ->
        mobitruAttachmentService.attachToTestItem(testItem, launchEntity, attribute));
  }
}
