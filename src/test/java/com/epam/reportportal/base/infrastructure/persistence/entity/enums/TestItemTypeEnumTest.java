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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class TestItemTypeEnumTest {

  private Map<TestItemTypeEnum, List<String>> allowed;
  private List<String> disallowed;

  @BeforeEach
  void setUp() throws Exception {
    allowed = Arrays.stream(TestItemTypeEnum.values())
        .collect(Collectors.toMap(it -> it,
            it -> Arrays.asList(it.name(), it.name().toUpperCase(), it.name().toLowerCase())));
    disallowed = Arrays.asList("noSuchIssueGroup", "", " ", null);
  }

  @Test
  void fromValue() {
    allowed.forEach((key, value) -> value.forEach(val -> {
      final Optional<TestItemTypeEnum> optional = TestItemTypeEnum.fromValue(val);
      assertTrue(optional.isPresent());
      assertEquals(key, optional.get());
    }));
    disallowed.forEach(it -> assertFalse(TestItemTypeEnum.fromValue(it).isPresent()));
  }

  @Test
  void sameLevel() {
    final TestItemTypeEnum suite = TestItemTypeEnum.SUITE;
    final TestItemTypeEnum story = TestItemTypeEnum.STORY;
    final TestItemTypeEnum scenario = TestItemTypeEnum.SCENARIO;
    final TestItemTypeEnum step = TestItemTypeEnum.STEP;
    assertTrue(suite.sameLevel(story));
    assertFalse(suite.sameLevel(scenario));
    assertFalse(suite.sameLevel(step));

  }

  @Test
  void higherThan() {
    final TestItemTypeEnum suite = TestItemTypeEnum.SUITE;
    final TestItemTypeEnum story = TestItemTypeEnum.STORY;
    final TestItemTypeEnum scenario = TestItemTypeEnum.SCENARIO;
    final TestItemTypeEnum step = TestItemTypeEnum.STEP;
    assertTrue(suite.higherThan(step));
    assertTrue(scenario.higherThan(step));
    assertFalse(suite.higherThan(story));
    assertFalse(step.higherThan(scenario));
    assertFalse(scenario.higherThan(story));
  }

  @Test
  void lowerThan() {
    final TestItemTypeEnum suite = TestItemTypeEnum.SUITE;
    final TestItemTypeEnum story = TestItemTypeEnum.STORY;
    final TestItemTypeEnum scenario = TestItemTypeEnum.SCENARIO;
    final TestItemTypeEnum step = TestItemTypeEnum.STEP;
    assertTrue(step.lowerThan(scenario));
    assertTrue(scenario.lowerThan(story));
    assertFalse(story.lowerThan(suite));
    assertFalse(scenario.lowerThan(step));
  }
}
