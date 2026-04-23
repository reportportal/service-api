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

package com.epam.ta.reportportal.core.launch.attribute.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.attribute.AttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchTypeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.stereotype.Component;

/**
 * Sets {@link Launch#getLaunchType()} from the system attribute {@code isAgentic} at launch start
 * only when that attribute is present. Otherwise, the existing {@code launchType} is default.
 * Launch type can not be changed on update.
 */
@Component
public class LaunchTypeAttributeHandler implements AttributeHandler {

  private final static String IS_AGENTIC_KEY = "isAgentic";

  @Override
  public void handleLaunchStart(Launch launch) {
    if (launch == null || launch.getAttributes() == null) {
      return;
    }
    for (ItemAttribute attribute : launch.getAttributes()) {
      if (Boolean.TRUE.equals(attribute.isSystem()) && IS_AGENTIC_KEY.equalsIgnoreCase(
          attribute.getKey())) {
        launch.setLaunchType(
            Boolean.parseBoolean(attribute.getValue()) ? LaunchTypeEnum.AGENTIC
                : LaunchTypeEnum.AUTOMATION);
        return;
      }
    }
  }

  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
    // launch_type is immutable after create; isAgentic on update is ignored
  }
}
