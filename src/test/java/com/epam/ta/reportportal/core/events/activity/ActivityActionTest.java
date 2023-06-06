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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.activity.ActivityAction;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
class ActivityActionTest {

  @Test
  void fromString() {
    List<ActivityAction> values = Arrays.stream(ActivityAction.values())
        .collect(Collectors.toList());
    List<String> strings = values.stream().map(ActivityAction::getValue)
        .collect(Collectors.toList());
    assertEquals(values.size(), strings.size());
    for (int i = 0; i < strings.size(); i++) {
      Optional<ActivityAction> type = ActivityAction.fromString(strings.get(i));
      assertTrue(type.isPresent());
      assertEquals(type.get(), values.get(i));
    }
    assertFalse(ActivityAction.fromString("no_such_activity").isPresent());
  }

}