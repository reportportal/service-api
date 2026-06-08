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

import com.epam.reportportal.base.core.launch.attribute.AttributeHandler;
import com.epam.reportportal.base.core.log.MobitruAttachmentService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * On launch finish, scans merged attributes for Mobitru recording keys (case-sensitive). For every
 * non-empty value a system log of type {@code mobitru} is created and attached to the launch. No-op
 * when the Mobitru plugin is not currently loaded and enabled.
 */
@Component
@RequiredArgsConstructor
public class MobitruLaunchAttributeHandler implements AttributeHandler {

  private final MobitruAttachmentService mobitruAttachmentService;

  @Override
  public void handleLaunchStart(Launch launch) {
    //not supported
  }

  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
    //not supported
  }

  @Override
  public void handleLaunchFinish(Launch launch) {
    if (launch == null || !mobitruAttachmentService.isPluginAvailable()) {
      return;
    }
    mobitruAttachmentService.extractRecordingAttributes(launch.getAttributes())
        .forEach(attribute -> mobitruAttachmentService.attachToLaunch(launch, attribute));
  }
}
