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
package com.epam.ta.reportportal.core.imprt.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ParseResults {

  private Instant startTime;

  private long duration;

  ParseResults() {
    startTime = Instant.now();
  }

  public ParseResults(Instant startTime, long duration) {
    this.startTime = startTime;
    this.duration = duration;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public long getDuration() {
    return duration;
  }

  void checkAndSetStartLaunchTime(Instant startSuiteTime) {
    if (this.startTime.isAfter(startSuiteTime)) {
      this.startTime = startSuiteTime;
    }
  }

  void increaseDuration(long duration) {
    this.duration += duration;
  }

  public Instant getEndTime() {
    return startTime.plus(duration, ChronoUnit.MILLIS);
  }
}
