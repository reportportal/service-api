/*
 * Copyright 2023 EPAM Systems
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

import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.MarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.events.activity.UnmarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.launch.attribute.AttributeHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Ivan Kustau
 */
@Component
public class RetentionPolicyAttributeHandler implements AttributeHandler {

  private final MessageBus messageBus;

  @Autowired
  public RetentionPolicyAttributeHandler(MessageBus messageBus) {
    this.messageBus = messageBus;
  }

  /**
   * Handles cases when retentionPolicy attribute is passed at the start.
   *
   * @param launch Launch that should be handled
   */
  public void handleLaunchStart(Launch launch) {
    if (launch == null || launch.getAttributes() == null) {
      return;
    }

    Set<ItemAttribute> attributes = launch.getAttributes();
    ItemAttribute importantAttribute = null;
    ItemAttribute regularAttribute = null;

    for (ItemAttribute attribute : attributes) {
      if (attribute.isSystem() && "retentionPolicy".equals(attribute.getKey())) {
        if ("important".equalsIgnoreCase(attribute.getValue())) {
          importantAttribute = attribute;
        } else if ("regular".equalsIgnoreCase(attribute.getValue())) {
          regularAttribute = attribute;
        }
      }
    }

    if (importantAttribute != null && regularAttribute != null) {
      // If both 'important' and 'regular' attributes are present, remove 'regular'
      attributes.remove(regularAttribute);
      launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    } else if (importantAttribute != null) {
      // If only 'important' attribute is present
      launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
    } else {
      // If only 'regular' attribute is present or neither is present
      launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
    }
  }

  /**
   * Handles cases when retentionPolicy attribute is passed at the update.
   *
   * @param launch Launch that should be handled
   */
  @Override
  public void handleLaunchUpdate(Launch launch, ReportPortalUser user) {
    Set<ItemAttribute> itemAttributes = launch.getAttributes();
    ItemAttribute retentionPolicyOldAttribute = null;
    ItemAttribute retentionPolicyNewAttribute = null;

    for (ItemAttribute attribute : itemAttributes) {
      if ("retentionPolicy".equalsIgnoreCase(attribute.getKey())) {
        if (attribute.isSystem()) {
          retentionPolicyOldAttribute = attribute;
        } else {
          retentionPolicyNewAttribute = attribute;
        }
      }
    }

    if (retentionPolicyNewAttribute != null) {
      itemAttributes.remove(retentionPolicyOldAttribute);
      retentionPolicyNewAttribute.setSystem(true);
      itemAttributes.add(retentionPolicyNewAttribute);

      if ("important".equalsIgnoreCase(retentionPolicyNewAttribute.getValue())) {
        launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
        messageBus.publishActivity(
            new MarkLaunchAsImportantEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(),
                user.getUsername()
            ));
      } else if ("regular".equalsIgnoreCase(retentionPolicyNewAttribute.getValue())) {
        launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
        messageBus.publishActivity(
            new UnmarkLaunchAsImportantEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(),
                user.getUsername()
            ));
      }
    }
  }
}
