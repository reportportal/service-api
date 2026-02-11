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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public enum TestItemTypeEnum implements Comparable<TestItemTypeEnum> {

  SUITE(Constants.SUITE_LEVEL, true),
  STORY(Constants.SUITE_LEVEL, true),
  TEST(Constants.TEST_LEVEL, true),
  SCENARIO(Constants.TEST_LEVEL, true),
  STEP(Constants.STEP_LEVEL, true),
  BEFORE_CLASS(Constants.STEP_LEVEL, false),
  BEFORE_GROUPS(Constants.STEP_LEVEL, false),
  BEFORE_METHOD(Constants.STEP_LEVEL, false),
  BEFORE_SUITE(Constants.TEST_LEVEL, false),
  BEFORE_TEST(Constants.STEP_LEVEL, false),
  AFTER_CLASS(Constants.STEP_LEVEL, false),
  AFTER_GROUPS(Constants.STEP_LEVEL, false),
  AFTER_METHOD(Constants.STEP_LEVEL, false),
  AFTER_SUITE(Constants.TEST_LEVEL, false),
  AFTER_TEST(Constants.STEP_LEVEL, false);

  /**
   * Level Comparator for TestItem types. Returns TRUE of level of first object is <b>lower</b> than level of second
   * object
   *
   * @author Andrei Varabyeu
   */
  private static final Comparator<TestItemTypeEnum> LEVEL_COMPARATOR = (o1, o2) -> Integer.compare(
      o2.level, o1.level);
  private int level;
  private boolean awareStatistics;

  TestItemTypeEnum(int level, boolean awareStatistics) {
    this.level = level;
    this.awareStatistics = awareStatistics;
  }

  public static Optional<TestItemTypeEnum> fromValue(String value) {
    return Arrays.stream(TestItemTypeEnum.values())
        .filter(type -> type.name().equalsIgnoreCase(value)).findAny();
  }

  public boolean sameLevel(TestItemTypeEnum other) {
    return 0 == LEVEL_COMPARATOR.compare(this, other);
  }

  /**
   * Is level of current item higher than level of specified
   *
   * @param type Item to compare
   * @return
   */
  public boolean higherThan(TestItemTypeEnum type) {
    return LEVEL_COMPARATOR.compare(this, type) > 0;
  }

  /**
   * Is level of current item lower than level of specified
   *
   * @param type Item to compare
   * @return
   */
  public boolean lowerThan(TestItemTypeEnum type) {
    return LEVEL_COMPARATOR.compare(this, type) < 0;
  }

  public boolean awareStatistics() {
    return awareStatistics;
  }

  public int getLevel() {
    return level;
  }

  public static class Constants {

    public static final int SUITE_LEVEL = 0;
    public static final int TEST_LEVEL = 1;
    public static final int STEP_LEVEL = 2;
  }
}
