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

import java.util.Arrays;
import java.util.Optional;

/**
 * How items are chosen when running auto-analyzer (all launches, by name, current only, etc.).
 *
 * @author Pavel Bortnik
 */
public enum AnalyzeMode {

  ALL_LAUNCHES("ALL"),
  BY_LAUNCH_NAME("LAUNCH_NAME"),
  CURRENT_LAUNCH("CURRENT_LAUNCH"),
  PREVIOUS_LAUNCH("PREVIOUS_LAUNCH"),
  CURRENT_AND_THE_SAME_NAME("CURRENT_AND_THE_SAME_NAME");

  private String value;

  AnalyzeMode(String value) {
    this.value = value;
  }

  public static Optional<AnalyzeMode> fromString(String mode) {
    return Arrays.stream(AnalyzeMode.values()).filter(it -> it.getValue().equalsIgnoreCase(mode))
        .findFirst();
  }

  public String getValue() {
    return value;
  }
}
