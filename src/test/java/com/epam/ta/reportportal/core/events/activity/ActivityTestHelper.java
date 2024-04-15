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

package com.epam.ta.reportportal.core.events.activity;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import java.time.Instant;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityTestHelper {

  static void checkActivity(Activity expected, Activity actual) {
    assertThat(actual).isEqualToIgnoringGivenFields(expected, "details", "createdAt");
    assertThat(actual.getObjectName()).isEqualTo(expected.getObjectName());
    checkActivityDetails(expected.getDetails(), actual.getDetails());
    checkCreatedAt(expected.getCreatedAt(), actual.getCreatedAt());
  }

  private static void checkActivityDetails(ActivityDetails expected, ActivityDetails actual) {
    assertThat(actual.getHistory()).containsExactlyInAnyOrderElementsOf(expected.getHistory());
  }

  private static void checkCreatedAt(Instant expected, Instant actual) {
    assertThat(expected.minusMillis(50).isBefore(actual)
        && expected.plusMillis(50).isAfter(actual)).isTrue();
  }
}
