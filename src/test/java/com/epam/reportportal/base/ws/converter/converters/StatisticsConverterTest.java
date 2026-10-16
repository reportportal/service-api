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

package com.epam.reportportal.base.ws.converter.converters;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchStatistics;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import com.epam.reportportal.base.reporting.StatisticsResource;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StatisticsConverterTest {

  @Test
  void toResourceConvertsLaunchStatistics() {
    StatisticsResource resource = StatisticsConverter.TO_RESOURCE.apply(Set.of(
        new LaunchStatistics(new StatisticsField("statistics$executions$total"), 3),
        new LaunchStatistics(new StatisticsField("statistics$defects$product_bug$total"), 1)
    ));

    assertThat(resource.getExecutions()).containsEntry("total", 3);
    assertThat(resource.getDefects()).containsEntry("product_bug", Set.of("total").stream()
        .collect(java.util.stream.Collectors.toMap(it -> it, it -> 1)));
  }
}
