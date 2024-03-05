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

package com.epam.ta.reportportal.core.analyzer.pattern.service.impl;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.pattern.service.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.ta.reportportal.ws.converter.builders.PatternTemplateBuilder;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class CreatePatternTemplateHandlerImpl implements CreatePatternTemplateHandler {

  protected final PatternTemplateRepository patternTemplateRepository;

  @Autowired
  public CreatePatternTemplateHandlerImpl(PatternTemplateRepository patternTemplateRepository) {
    this.patternTemplateRepository = patternTemplateRepository;
  }

  @Override
  public PatternTemplate createPatternTemplate(Long projectId,
      CreatePatternTemplateRQ createPatternTemplateRQ) {
    final String name = StringUtils.trim(createPatternTemplateRQ.getName());
    BusinessRule.expect(
        patternTemplateRepository.existsByProjectIdAndNameIgnoreCase(projectId, name),
        equalTo(false)
    ).verify(ErrorType.RESOURCE_ALREADY_EXISTS, name);
    PatternTemplate patternTemplate =
        new PatternTemplateBuilder().withCreateRequest(createPatternTemplateRQ).withName(name)
            .withProjectId(projectId).get();
    return patternTemplateRepository.save(patternTemplate);
  }
}
