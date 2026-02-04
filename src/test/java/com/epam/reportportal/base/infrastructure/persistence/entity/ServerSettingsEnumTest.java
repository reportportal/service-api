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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettingsEnum;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ServerSettingsEnumTest {

  private Map<ServerSettingsEnum, List<String>> allowed;
  private List<String> disallowed;

  @BeforeEach
  void setUp() {
    allowed = Arrays.stream(ServerSettingsEnum.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.getAttribute(), it.getAttribute().toUpperCase(),
                it.getAttribute().toLowerCase())
        ));
    disallowed = Arrays.asList("noSuchAttribute", "", " ", null);
  }

  @Test
  void findByAttribute() {
    allowed.forEach((key, value) -> value.forEach(val -> {
      final Optional<ServerSettingsEnum> optional = ServerSettingsEnum.findByAttribute(val);
      assertTrue(optional.isPresent());
      assertEquals(key, optional.get());
    }));
    disallowed.forEach(it -> assertFalse(ServerSettingsEnum.findByAttribute(it).isPresent()));
  }

  @Test
  void isPresent() {
    allowed.entrySet().stream().flatMap(it -> it.getValue().stream())
        .forEach(it -> assertTrue(ServerSettingsEnum.isPresent(it)));
    disallowed.forEach(it -> assertFalse(ServerSettingsEnum.isPresent(it)));
  }
}
