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

package com.epam.reportportal.infrastructure.persistence.entity.widget.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopPatternTemplatesContent implements Serializable {

  @JsonProperty("attributeValue")
  private String attributeValue;

  @JsonProperty(value = "patterns")
  private List<PatternTemplateStatistics> patternTemplates = Lists.newArrayList();

  public TopPatternTemplatesContent() {
  }

  public TopPatternTemplatesContent(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  public List<PatternTemplateStatistics> getPatternTemplates() {
    return patternTemplates;
  }

  public void setPatternTemplates(List<PatternTemplateStatistics> patternTemplates) {
    this.patternTemplates = patternTemplates;
  }
}
