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

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateTestItemKey implements Serializable {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pattern_id")
  private PatternTemplate patternTemplate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private TestItem testItem;

  public PatternTemplateTestItemKey() {
  }

  public PatternTemplate getPatternTemplate() {
    return patternTemplate;
  }

  public void setPatternTemplate(PatternTemplate patternTemplate) {
    this.patternTemplate = patternTemplate;
  }

  public TestItem getTestItem() {
    return testItem;
  }

  public void setTestItem(TestItem testItem) {
    this.testItem = testItem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatternTemplateTestItemKey that = (PatternTemplateTestItemKey) o;
    return Objects.equals(patternTemplate, that.patternTemplate) && Objects.equals(testItem,
        that.testItem);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patternTemplate, testItem);
  }
}
