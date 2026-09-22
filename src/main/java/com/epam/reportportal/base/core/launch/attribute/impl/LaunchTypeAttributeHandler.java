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

package com.epam.reportportal.base.core.launch.attribute.impl;


import com.epam.reportportal.base.core.launch.attribute.AttributeHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Sets {@link Launch#getLaunchType()} from the system attributes {@code isAgentic} or {@code isPipeline} at launch
 * start only when one of those attributes is present. Otherwise, the existing {@code launchType} is default. Launch
 * type can not be changed on update.
 */
@Component
public class LaunchTypeAttributeHandler implements AttributeHandler {

  private static final String IS_AGENTIC_KEY = "isAgentic";
  private static final String IS_PIPELINE_KEY = "isPipeline";

  @Override
  public void handleLaunchStart(Launch launch) {
    if (launch == null || CollectionUtils.isEmpty(launch.getAttributes())) {
      return;
    }
    launch.getAttributes().stream()
        .filter(attribute -> Boolean.TRUE.equals(attribute.isSystem()))
        .map(this::resolveLaunchType)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(launch::setLaunchType);
  }

  private LaunchTypeEnum resolveLaunchType(ItemAttribute attribute) {
    boolean enabled = Boolean.parseBoolean(attribute.getValue());
    if (IS_AGENTIC_KEY.equalsIgnoreCase(attribute.getKey())) {
      return enabled ? LaunchTypeEnum.AGENTIC : LaunchTypeEnum.AUTOMATION;
    }
    if (IS_PIPELINE_KEY.equalsIgnoreCase(attribute.getKey())) {
      return enabled ? LaunchTypeEnum.PIPELINE : LaunchTypeEnum.AUTOMATION;
    }
    return null;
  }

  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
    // launch_type is immutable after create; isAgentic/isPipeline on update is ignored
  }
}
