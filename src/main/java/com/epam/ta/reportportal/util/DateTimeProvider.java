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

package com.epam.ta.reportportal.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Class to ease writing/testing of date-based logic.
 *
 * @author Dzianis_Shybeka
 */
public class DateTimeProvider {

  public LocalDateTime localDateTimeNow() {
    return LocalDateTime.now();
  }

  public static Instant instantNow() {
    return Instant.now().truncatedTo(ChronoUnit.MICROS);
  }
}
