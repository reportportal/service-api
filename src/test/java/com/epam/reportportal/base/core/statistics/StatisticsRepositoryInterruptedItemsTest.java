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

package com.epam.reportportal.base.core.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.statistics.repository.StatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/fill/item/items-fill.sql")
class StatisticsRepositoryInterruptedItemsTest extends BaseMvcTest {

  @Autowired
  private StatisticsRepository statisticsRepository;

  @Autowired
  private TestItemRepository testItemRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void selectInterruptedItemsSkipsItemsThatAlreadyHaveStatistics() {
    final Long launchId = 1L;
    testItemRepository.interruptInProgressItems(launchId);

    var interrupted = statisticsRepository.selectInterruptedItems(launchId);
    assertFalse(interrupted.isEmpty(), "Fixture should contain interrupted leaf items");

    Long counted = interrupted.get(0).getItemId();
    jdbcTemplate.update(
        "INSERT INTO statistics_field (name) VALUES ('statistics$executions$failed') ON CONFLICT DO NOTHING");
    jdbcTemplate.update("INSERT INTO statistics (s_counter, statistics_field_id, item_id) VALUES "
        + "(1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$failed'), ?)",
        counted);

    List<Long> remaining = statisticsRepository.selectInterruptedItems(launchId).stream()
        .map(it -> it.getItemId()).toList();
    assertFalse(remaining.contains(counted));
    assertEquals(interrupted.size() - 1, remaining.size());
  }

  @Test
  void selectInterruptedItemsKeepsItemsThatHaveOnlyDefectStatistics() {
    final Long launchId = 1L;
    testItemRepository.interruptInProgressItems(launchId);

    var interrupted = statisticsRepository.selectInterruptedItems(launchId);
    assertFalse(interrupted.isEmpty(), "Fixture should contain interrupted leaf items");

    Long withDefect = interrupted.get(0).getItemId();
    jdbcTemplate.update("INSERT INTO statistics (s_counter, statistics_field_id, item_id) VALUES "
        + "(1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$defects$to_investigate$total'), ?)",
        withDefect);

    List<Long> remaining = statisticsRepository.selectInterruptedItems(launchId).stream()
        .map(it -> it.getItemId()).toList();
    assertTrue(remaining.contains(withDefect));
    assertEquals(interrupted.size(), remaining.size());
  }
}
