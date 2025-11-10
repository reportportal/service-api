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

package com.epam.reportportal.infrastructure.persistence.entity;

import java.util.Arrays;
import java.util.Optional;

/**
 * Statistics calculation strategy
 *
 * @author Andrei Varabyeu
 */
public enum StatisticsCalculationStrategy {

  /**
   * Step based strategy. Only steps should be calculated as statistics items
   */
  STEP_BASED,

  /**
   * All (including befores and afters) should be calculated as statistics items
   */
  ALL_ITEMS_BASED,

  /**
   * Optimized for BDD-based launches. Does NOT calculates stats for step/scenario level, only starting from TEST level
   */
  TEST_BASED;

  /**
   * Loads strategy by it's string name. Case matters.
   *
   * @param strategy Strategy string
   * @return Optional of found enum value
   */
  public static Optional<StatisticsCalculationStrategy> fromString(String strategy) {
    return Arrays.stream(values()).filter(s -> s.name().equals(strategy)).findAny();
  }

}
