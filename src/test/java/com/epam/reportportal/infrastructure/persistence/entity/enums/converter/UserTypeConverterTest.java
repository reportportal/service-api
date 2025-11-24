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

import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UserTypeConverterTest extends AttributeConverterTest {

  @BeforeEach
  void setUp() throws Exception {
    this.converter = new UserTypeConverter();
    allowedValues = Arrays.stream(UserType.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.name(), it.name().toUpperCase(), it.name().toLowerCase())));
  }

  @Override
  protected void convertToColumnTest() {
    Arrays.stream(UserType.values())
        .forEach(it -> assertEquals(it.name(), converter.convertToDatabaseColumn(it)));
  }
}
