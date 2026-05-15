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

import com.epam.reportportal.base.core.events.attachment.ExternalAttachmentLoadProducer;
import com.epam.reportportal.base.core.launch.attribute.AttributeHandler;
import com.epam.reportportal.base.core.log.SystemLogService;
import com.epam.reportportal.base.core.plugin.PluginAvailabilityChecker;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * On launch finish, scans merged attributes for the {@code MBID} key (case-sensitive). For every
 * non-empty value a system log of type {@code mobitru} is created and attached to the launch. No-op
 * when the Mobitru plugin is not currently loaded and enabled.
 */
@Component
@RequiredArgsConstructor
public class MobitruLaunchAttributeHandler implements AttributeHandler {

  public static final String MBID_KEY = "MBID";
  private static final String PLUGIN_ID = "mobitru";
  private static final String LOG_TYPE_NAME = "mobitru";

  private final PluginAvailabilityChecker pluginAvailabilityChecker;
  private final SystemLogService systemLogService;
  private final ExternalAttachmentLoadProducer externalAttachmentLoadProducer;

  @Override
  public void handleLaunchStart(Launch launch) {
  }

  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
  }

  @Override
  public void handleLaunchFinish(Launch launch) {
    if (!pluginAvailabilityChecker.isAvailable(PLUGIN_ID)) {
      return;
    }
    if (launch == null) {
      return;
    }
    Set<ItemAttribute> attributes = launch.getAttributes();
    if (attributes == null || attributes.isEmpty()) {
      return;
    }
    attributes.stream()
        .filter(attr -> MBID_KEY.equals(attr.getKey()))
        .map(ItemAttribute::getValue)
        .filter(value -> value != null && !value.isEmpty())
        .forEach(value -> {
          Long logId = systemLogService.writeLaunchLog(launch, LOG_TYPE_NAME, value);
          externalAttachmentLoadProducer.publish(logId, launch.getProjectId(), launch.getId(), null,
              value);
        });
  }
}
