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

package com.epam.reportportal.core.project.validator.attribute;

import com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum;

public class DelayBoundLessRule {

  private final ProjectAttributeEnum lower;
  private final ProjectAttributeEnum higher;

  public DelayBoundLessRule(ProjectAttributeEnum lower, ProjectAttributeEnum higher) {
    this.lower = lower;
    this.higher = higher;
  }

  public ProjectAttributeEnum getLower() {
    return lower;
  }

  public ProjectAttributeEnum getHigher() {
    return higher;
  }
}
