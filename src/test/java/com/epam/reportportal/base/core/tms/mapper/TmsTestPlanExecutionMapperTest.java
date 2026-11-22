/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import org.junit.jupiter.api.Test;

/**
 * Total and covered must land in their own fields on every construction path.
 */
class TmsTestPlanExecutionMapperTest {

  private final TmsTestPlanExecutionMapper mapper = new TmsTestPlanExecutionMapperImpl();

  @Test
  void builder_shouldSetTotalAndCoveredByName() {
    var statistic = TmsTestPlanExecutionStatistic.builder().total(7L).covered(3L).build();

    assertEquals(7L, statistic.getTotal());
    assertEquals(3L, statistic.getCovered());

    var reversedOrder = TmsTestPlanExecutionStatistic.builder().covered(3L).total(7L).build();

    assertEquals(statistic, reversedOrder);
  }

  @Test
  void createEmptyStatistics_shouldBeZeroTotalAndZeroCovered() {
    var statistic = mapper.createEmptyStatistics();

    assertEquals(0L, statistic.getTotal());
    assertEquals(0L, statistic.getCovered());
  }

  @Test
  void toDto_shouldKeepTotalAndCovered() {
    var dto = mapper.toDto(TmsTestPlanExecutionStatistic.builder().total(7L).covered(3L).build());

    assertEquals(7L, dto.getTotal());
    assertEquals(3L, dto.getCovered());
  }
}
