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

package com.epam.reportportal.base.infrastructure.persistence.entity.pattern;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lightweight DTO for pattern-to-test-item matches in widgets and search.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateTestItemPojo implements Serializable {

  private Long patternTemplateId;

  private Long testItemId;

  public PatternTemplateTestItemPojo() {
  }

  public PatternTemplateTestItemPojo(Long patternTemplateId, Long testItemId) {
    this.patternTemplateId = patternTemplateId;
    this.testItemId = testItemId;
  }

  public Long getPatternTemplateId() {
    return patternTemplateId;
  }

  public void setPatternTemplateId(Long patternTemplateId) {
    this.patternTemplateId = patternTemplateId;
  }

  public Long getTestItemId() {
    return testItemId;
  }

  public void setTestItemId(Long testItemId) {
    this.testItemId = testItemId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatternTemplateTestItemPojo that = (PatternTemplateTestItemPojo) o;
    return Objects.equals(patternTemplateId, that.patternTemplateId) && Objects.equals(testItemId,
        that.testItemId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patternTemplateId, testItemId);
  }
}
