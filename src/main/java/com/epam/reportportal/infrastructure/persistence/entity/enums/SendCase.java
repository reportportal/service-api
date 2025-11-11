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

package com.epam.reportportal.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Email notification cases enumerator for project settings
 *
 * @author Andrei_Ramanchuk
 */
public enum SendCase {

  //@formatter:off
  ALWAYS("always"),
  FAILED("failed"),
  TO_INVESTIGATE("toInvestigate"),
  MORE_10("more10"),
  MORE_20("more20"),
  MORE_50("more50");
  //@formatter:on

  private final String value;

  SendCase(String value) {
    this.value = value;
  }

  public static Optional<SendCase> findByName(String name) {
    return Arrays.stream(SendCase.values())
        .filter(val -> val.getCaseString().equalsIgnoreCase(name)).findAny();
  }

  public static boolean isPresent(String name) {
    return findByName(name).isPresent();
  }

  public String getCaseString() {
    return value;
  }
}
