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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
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
class TestItemIssueGroupTest {

  private Map<TestItemIssueGroup, List<String>> allowed;
  private List<String> disallowed;

  @BeforeEach
  void setUp() {
    allowed = Arrays.stream(TestItemIssueGroup.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.getValue(), it.getValue().toUpperCase(),
                it.getValue().toLowerCase())
        ));
    disallowed = Arrays.asList("noSuchIssueGroup", "", " ", null);
  }

  @Test
  void fromValue() {
    allowed.forEach((key, value) -> value.forEach(val -> {
      final Optional<TestItemIssueGroup> optional = TestItemIssueGroup.fromValue(val);
      assertTrue(optional.isPresent());
      assertEquals(key, optional.get());
    }));
    disallowed.forEach(it -> assertFalse(TestItemIssueGroup.fromValue(it).isPresent()));
  }

  @Test
  void validate() {
    allowed.forEach(
        (key, value) -> value.forEach(val -> assertEquals(key, TestItemIssueGroup.validate(val))));
    disallowed.forEach(it -> assertNull(TestItemIssueGroup.validate(it)));
  }

  @Test
  void validValues() {
    final List<String> strings = TestItemIssueGroup.validValues();
    assertEquals(strings,
        Arrays.stream(TestItemIssueGroup.values()).map(TestItemIssueGroup::getValue)
            .collect(Collectors.toList()));
  }
}
