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

package com.epam.reportportal.core.analyzer.pattern.service;

import com.epam.reportportal.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.reportportal.infrastructure.persistence.entity.pattern.PatternTemplate;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface CreatePatternTemplateHandler {

  /**
   * Create {@link com.epam.reportportal.infrastructure.persistence.entity.pattern.PatternTemplate} entity for specified
   * {@link com.epam.reportportal.infrastructure.persistence.entity.project.Project}
   *
   * @param projectId               {@link
   *                                com.epam.reportportal.infrastructure.persistence.entity.pattern.PatternTemplate}
   * @param createPatternTemplateRQ {@link CreatePatternTemplateRQ}
   * @return {@link java.util.regex.Pattern}
   */
  PatternTemplate createPatternTemplate(Long projectId,
      CreatePatternTemplateRQ createPatternTemplateRQ);
}
