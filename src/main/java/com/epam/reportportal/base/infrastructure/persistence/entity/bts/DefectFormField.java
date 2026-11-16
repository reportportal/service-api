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

package com.epam.reportportal.base.infrastructure.persistence.entity.bts;

import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Set;

/**
 * Describes one field in a BTS defect submission form (id, type, required, allowed values).
 *
 * @author Pavel Bortnik
 */
public class DefectFormField implements Serializable {

  private String fieldId;

  private String type;

  private boolean isRequired;

  private Set<String> values;

  private Set<DefectFieldAllowedValue> defectFieldAllowedValues = Sets.newHashSet();

  public DefectFormField() {
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean required) {
    isRequired = required;
  }

  public Set<String> getValues() {
    return values;
  }

  public void setValues(Set<String> values) {
    this.values = values;
  }

  public Set<DefectFieldAllowedValue> getDefectFieldAllowedValues() {
    return defectFieldAllowedValues;
  }

  public void setDefectFieldAllowedValues(Set<DefectFieldAllowedValue> defectFieldAllowedValues) {
    this.defectFieldAllowedValues.clear();
    this.defectFieldAllowedValues.addAll(defectFieldAllowedValues);
  }
}
