/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternTemplateBuilderTest {

  @Test
  void patternTemplateBuilder() {
    CreatePatternTemplateRQ createPatternTemplateRQ = new CreatePatternTemplateRQ();
    String name = "name";
    String type = "STRING";
    boolean enabled = true;
    String value = "qwe";
    createPatternTemplateRQ.setName(name);
    createPatternTemplateRQ.setType(type);
    createPatternTemplateRQ.setEnabled(enabled);
    createPatternTemplateRQ.setValue(value);

    PatternTemplate patternTemplate = new PatternTemplateBuilder().withCreateRequest(
        createPatternTemplateRQ).get();

    assertEquals(name, patternTemplate.getName());
    assertEquals(type, patternTemplate.getTemplateType().name());
    assertEquals(enabled, patternTemplate.isEnabled());
    assertEquals(value, patternTemplate.getValue());
  }
}