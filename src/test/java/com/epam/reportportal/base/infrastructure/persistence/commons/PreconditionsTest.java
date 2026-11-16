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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PreconditionsTest {

  @Test
  void hasModePositive() {

    LaunchModeEnum mode = LaunchModeEnum.DEFAULT;

    Assertions.assertTrue(Preconditions.hasMode(mode).test(filterConditionWithMode(mode)));
  }

  @Test
  void hasModeNegative() {

    LaunchModeEnum mode = LaunchModeEnum.DEFAULT;
    LaunchModeEnum anotherMode = LaunchModeEnum.DEBUG;

    Assertions.assertFalse(Preconditions.hasMode(mode).test(filterConditionWithMode(anotherMode)));
  }

  @Test
  void sameTime() {

    Instant date = Instant.now();
    Instant sameTime = Instant.now();

    Assertions.assertTrue(Preconditions.sameTimeOrLater(date).test(sameTime));
  }

  @Test
  void laterTime() {

    Instant date = Instant.now();

    Instant laterTime = Instant.now().minusSeconds(1L);

    Assertions.assertTrue(Preconditions.sameTimeOrLater(laterTime).test(date));
  }

  @Test
  void beforeTime() {

    Instant date = Instant.now();

    Instant beforeTime = Instant.now().plusSeconds(1L);

    Assertions.assertFalse(Preconditions.sameTimeOrLater(beforeTime).test(date));
  }

  @Test
  void validateNullValue() {

    assertThrows(NullPointerException.class,
        () -> Preconditions.sameTimeOrLater(null).test(Instant.now()));
  }

  private FilterCondition filterConditionWithMode(LaunchModeEnum mode) {

    return new FilterCondition(Condition.EQUALS, false, mode.name(), CRITERIA_LAUNCH_MODE);
  }

}
