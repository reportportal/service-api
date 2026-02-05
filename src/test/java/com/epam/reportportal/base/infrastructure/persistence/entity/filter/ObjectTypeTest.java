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

package com.epam.reportportal.base.infrastructure.persistence.entity.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ObjectTypeTest {

  private Map<ObjectType, List<String>> allowed;
  private List<String> disallowed;

  @BeforeEach
  void setUp() {
    allowed = Arrays.stream(ObjectType.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.name(), it.name().toUpperCase(), it.name().toLowerCase())));
    disallowed = Arrays.asList("noSuchObjectType", "", " ", null);
  }

  @Test
  void getObjectTypeByName() {
    allowed.forEach((key, value) -> value.forEach(
        val -> assertEquals(key, ObjectType.getObjectTypeByName(val))));
  }

  @Test
  void getObjectTypeByNameFail() {
    Collections.shuffle(disallowed);
    assertThrows(ReportPortalException.class,
        () -> ObjectType.getObjectTypeByName(disallowed.get(0)));
  }

  @Test
  void getTypeByName() {
    allowed.forEach((key, value) -> value.forEach(
        val -> assertEquals(key.getClassObject(), ObjectType.getTypeByName(val))));
  }

  @Test
  void getTypeByNameFail() {
    Collections.shuffle(disallowed);
    assertThrows(ReportPortalException.class, () -> ObjectType.getTypeByName(disallowed.get(0)));
  }
}
