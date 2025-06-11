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

import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.core.settings.ImportantLaunchSettingHandler.IMPORTANT_SETTINGS_KEY;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.MarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.events.activity.UnmarkLaunchAsImportantEvent;
import com.epam.ta.reportportal.core.launch.attribute.AttributeHandler;
import com.epam.ta.reportportal.core.settings.ServerSettingsService;
import com.epam.ta.reportportal.core.project.ProjectService;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Ivan Kustau
 */
@Component
@RequiredArgsConstructor
public class RetentionPolicyAttributeHandler implements AttributeHandler {

  private final MessageBus messageBus;
  private final ProjectService projectService;

  private final ServerSettingsService serverSettingsService;

  /**
   * Handles cases when retentionPolicy attribute is passed at the start.
   *
   * @param launch Launch that should be handled
   */
  public void handleLaunchStart(Launch launch) {
    if (launch == null || launch.getAttributes() == null) {
      return;
    }

    if (serverSettingsService.checkServerSettingsState(IMPORTANT_SETTINGS_KEY, Boolean.FALSE.toString())) {
      launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
      return;
    }

    Set<ItemAttribute> attributes = launch.getAttributes();
    ItemAttribute importantAttribute = null;
    ItemAttribute regularAttribute = null;

    for (ItemAttribute attribute : attributes) {
      if (attribute.isSystem() && RETENTION_POLICY_KEY.equals(attribute.getKey())) {
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
    if (serverSettingsService.checkServerSettingsState(IMPORTANT_SETTINGS_KEY,
        Boolean.FALSE.toString())) {
      throw new ReportPortalException(FORBIDDEN_OPERATION, "Feature is disabled");
    }
    if (launch == null || launch.getAttributes() == null) {
      return;
    }

    Set<ItemAttribute> itemAttributes = launch.getAttributes();
    ItemAttribute retentionPolicyOldAttribute = null;
    ItemAttribute retentionPolicyNewAttribute = null;

    for (ItemAttribute attribute : itemAttributes) {

      if (RETENTION_POLICY_KEY.equalsIgnoreCase(attribute.getKey())) {
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
      if (retentionPolicyOldAttribute != null && Objects.equals(
          retentionPolicyOldAttribute.getValue(), retentionPolicyNewAttribute.getValue())) {
        return;
      }
      Project project = projectService.findProjectById(launch.getProjectId());
      if ("important".equalsIgnoreCase(retentionPolicyNewAttribute.getValue())) {
        launch.setRetentionPolicy(RetentionPolicyEnum.IMPORTANT);
        messageBus.publishActivity(
            new MarkLaunchAsImportantEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(),
                user.getUsername(), project.getOrganizationId()
            ));
      } else if ("regular".equalsIgnoreCase(retentionPolicyNewAttribute.getValue())) {
        launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR);
        messageBus.publishActivity(
            new UnmarkLaunchAsImportantEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(),
                user.getUsername(), project.getOrganizationId()
            ));
      }
    }
  }
}
