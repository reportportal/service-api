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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.core.project.validator.attribute.DelayBoundLessRule;
import com.epam.reportportal.base.core.project.validator.attribute.DelayBoundValidator;
import com.epam.reportportal.base.core.project.validator.attribute.ProjectAttributeValidator;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectValidationConfig {

  @Bean
  public DelayBoundValidator delayBoundValidator() {
    return new DelayBoundValidator(getDelayBoundRules());
  }

  private List<DelayBoundLessRule> getDelayBoundRules() {
    return List.of(new DelayBoundLessRule(ProjectAttributeEnum.KEEP_SCREENSHOTS,
            ProjectAttributeEnum.KEEP_LOGS),
        new DelayBoundLessRule(ProjectAttributeEnum.KEEP_LOGS, ProjectAttributeEnum.KEEP_LAUNCHES)
    );
  }

  @Bean
  public ProjectAttributeValidator projectAttributeValidator() {
    return new ProjectAttributeValidator(delayBoundValidator());
  }
}
