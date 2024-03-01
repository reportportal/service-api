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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.bts.DefectFieldAllowedValue;
import com.epam.ta.reportportal.entity.bts.DefectFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationFieldsConverterTest {

  @Test
  void fieldToDb() {
    final PostFormField field = getField();
    final DefectFormField defectFormField = IntegrationFieldsConverter.FIELD_TO_DB.apply(field);

    assertEquals(defectFormField.getFieldId(), field.getId());
    assertEquals(defectFormField.getType(), field.getFieldType());
    assertEquals(defectFormField.isRequired(), field.getIsRequired());
    assertThat(defectFormField.getValues()).containsExactlyElementsOf(field.getValue());
    assertThat(defectFormField.getDefectFieldAllowedValues()).hasSameSizeAs(
        field.getDefinedValues());
  }

  @Test
  void fieldToModel() {
    final DefectFormField defectFormField = getDefectFormField();
    final PostFormField postFormField = IntegrationFieldsConverter.FIELD_TO_MODEL.apply(
        defectFormField);

    assertEquals(postFormField.getFieldType(), defectFormField.getType());
    assertEquals(postFormField.getIsRequired(), defectFormField.isRequired());
    assertThat(postFormField.getValue()).containsExactlyInAnyOrder("value1", "value2");
    assertThat(postFormField.getDefinedValues()).hasSameSizeAs(
        defectFormField.getDefectFieldAllowedValues());
  }

  private static PostFormField getField() {
    final PostFormField postFormField = new PostFormField();
    postFormField.setFieldType("type");
    postFormField.setId("id");
    postFormField.setIsRequired(true);
    postFormField.setFieldName("name");
    final AllowedValue allowedValue = new AllowedValue();
    allowedValue.setValueId("valueId");
    allowedValue.setValueName("valueName");
    postFormField.setDefinedValues(Collections.singletonList(allowedValue));
    postFormField.setValue(Collections.singletonList("value"));
    return postFormField;
  }

  private static DefectFormField getDefectFormField() {
    final DefectFormField defectFormField = new DefectFormField();
    defectFormField.setFieldId("fieldId");
    defectFormField.setRequired(true);
    defectFormField.setType("type");
    defectFormField.setValues(Sets.newHashSet("value1", "value2"));
    final DefectFieldAllowedValue defectFieldAllowedValue = new DefectFieldAllowedValue();
    defectFieldAllowedValue.setValueId("id");
    defectFieldAllowedValue.setValueName("name");
    defectFormField.setDefectFieldAllowedValues(Sets.newHashSet(defectFieldAllowedValue));
    return defectFormField;
  }
}