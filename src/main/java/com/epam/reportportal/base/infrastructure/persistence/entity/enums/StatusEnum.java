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
import java.util.Optional;

public enum StatusEnum {

  //@formatter:off
  IN_PROGRESS("", false),
  PASSED("passed", true),
  FAILED("failed", false),
  STOPPED("stopped", false), //status for manually stopped launches
  SKIPPED("skipped", false),
  INTERRUPTED("failed", false),
  //RESETED("reseted"), //status for items with deleted descendants
  CANCELLED("cancelled", false), //soupUI specific status
  INFO("info", true),
  WARN("warn", true),
  TO_RUN("to_run", false); // New status for manual test execution
  //@formatter:on

  private final String executionCounterField;

  private final boolean positive;

  StatusEnum(String executionCounterField, boolean isPositive) {
    this.executionCounterField = executionCounterField;
    this.positive = isPositive;
  }

  public static Optional<StatusEnum> fromValue(String value) {
    return Arrays.stream(StatusEnum.values())
        .filter(status -> status.name().equalsIgnoreCase(value)).findAny();
  }

  public static boolean isPresent(String name) {
    return fromValue(name).isPresent();
  }

  public String getExecutionCounterField() {
    return executionCounterField;
  }

  public boolean isPositive() {
    return positive;
  }
}
