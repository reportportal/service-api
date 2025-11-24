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

package com.epam.reportportal.infrastructure.persistence.entity.enums.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import jakarta.persistence.AttributeConverter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AttributeConverterTest {

  protected AttributeConverter converter;
  protected Map<Enum, List<String>> allowedValues;

  @Test
  void convertToDatabaseColumn() {
    convertToColumnTest();
  }

  @Test
  void convertToEntityAttribute() {
    allowedValues.forEach((key, value) -> value.forEach(
        it -> assertEquals(key, converter.convertToEntityAttribute(it))));
  }

  @Test
  void convertToEntityAttributeFail() {

    assertThrows(ReportPortalException.class, () -> converter.convertToEntityAttribute("wrong parameter"));
  }

  protected abstract void convertToColumnTest();
}
