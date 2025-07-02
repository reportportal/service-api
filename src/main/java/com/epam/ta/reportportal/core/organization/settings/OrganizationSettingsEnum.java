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

package com.epam.ta.reportportal.core.organization.settings;

import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import lombok.Getter;

/**
 * Enum that defines keys for organization-specific settings related to retention policies. These keys are used to
 * identify different retention settings in the database.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 **/
@Getter
public enum OrganizationSettingsEnum {

  RETENTION_LAUNCHES("retention_launches", ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute()),
  RETENTION_LOGS("retention_logs", ProjectAttributeEnum.KEEP_LOGS.getAttribute()),
  RETENTION_ATTACHMENTS("retention_attachments", ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute());

  private final String name;
  private final String projectFormatKey;

  OrganizationSettingsEnum(String name, String projectFormatKey) {
    this.name = name;
    this.projectFormatKey = projectFormatKey;
  }
}
