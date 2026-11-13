/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;

/**
 * Organization type converter. Converts from ordinal value to String representations
 *
 * @author Siarhei Hrabko
 */
public class OrganizationTypeConverterTest extends AttributeConverterTest {

  @BeforeEach
  void setUp() {
    this.converter = new OrganizationTypeConverter();
    allowedValues = Arrays.stream(OrganizationType.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.name(), it.name().toUpperCase(), it.name().toLowerCase())));
  }

  @Override
  protected void convertToColumnTest() {
    Arrays.stream(OrganizationType.values())
        .forEach(it -> assertEquals(it.name(), converter.convertToDatabaseColumn(it)));
  }
}
