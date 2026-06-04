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

/**
 * Get project information intervals<br> Available values:
 * <ul>
 * <li>1 week</li>
 * <li>3 months (by default)</li>
 * <li>6 months</li>
 * </ul>
 *
 * @author Andrei_Ramanchuk
 */
public enum InfoInterval {

  //@formatter:off
  ONE_MONTH("1M", 1),
  THREE_MONTHS("3M", 3),
  SIX_MONTHS("6M", 6);
  //@formatter:on

  private String interval;
  private Integer counter;

  InfoInterval(String value, Integer count) {
    this.interval = value;
    this.counter = count;
  }

  public static InfoInterval getByName(String name) {
    return InfoInterval.valueOf(name);
  }

  public static Optional<InfoInterval> findByInterval(String interval) {
    return Arrays.stream(InfoInterval.values())
        .filter(value -> value.getInterval().equalsIgnoreCase(interval)).findAny();
  }

  public String getInterval() {
    return interval;
  }

  public Integer getCount() {
    return counter;
  }
}
