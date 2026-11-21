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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import com.epam.reportportal.base.reporting.FinishTestItemRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.json.JsonMapper;

/**
 * Finishing a launch or a parent item finishes every IN_PROGRESS descendant, also when there are more of them than fit
 * into one chunk.
 */
@Sql("/db/launch/launch-fill.sql")
class FinishInProgressDescendantsTest extends BaseMvcTest {

  private static final long LAUNCH_ID = 3L;
  private static final String LAUNCH_UUID = "befef834-b2ef-4acf-aea3-b5a5b15fd93c";
  private static final long ROOT_ID = 10_000L;
  private static final String ROOT_UUID = "finish-descendants-root";
  private static final int SUITES_PER_LEVEL = 20;
  private static final int STEPS_PER_SUITE = 3;
  // 20 suites, each with 2 nested suites (60 suites), each nested suite with 3 steps (120 steps), all under one root
  private static final int SUITES = SUITES_PER_LEVEL * 3;
  private static final int STEPS = SUITES_PER_LEVEL * 2 * STEPS_PER_SUITE;

  @Autowired
  private JsonMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @PersistenceContext
  private EntityManager entityManager;

  @BeforeEach
  void createInProgressHierarchy() {
    long nextId = ROOT_ID;
    insertItem(nextId++, ROOT_UUID, "SUITE", true, null, String.valueOf(ROOT_ID));
    for (int i = 0; i < SUITES_PER_LEVEL; i++) {
      long suiteId = nextId++;
      String suitePath = ROOT_ID + "." + suiteId;
      insertItem(suiteId, null, "SUITE", true, ROOT_ID, suitePath);
      for (int j = 0; j < 2; j++) {
        long nestedId = nextId++;
        String nestedPath = suitePath + "." + nestedId;
        insertItem(nestedId, null, "SUITE", true, suiteId, nestedPath);
        for (int k = 0; k < STEPS_PER_SUITE; k++) {
          long stepId = nextId++;
          insertItem(stepId, null, "STEP", false, nestedId, nestedPath + "." + stepId);
        }
      }
    }
    assertEquals(SUITES + STEPS, countDescendants("IN_PROGRESS"));
  }

  @Test
  void finishLaunchFinishesAllInProgressDescendants() throws Exception {
    FinishExecutionRQ rq = new FinishExecutionRQ();
    rq.setEndTime(Instant.now());

    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/" + LAUNCH_UUID + "/finish")
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
    entityManager.flush();

    assertEquals(0, countItemsInProgress());
    assertEquals(STEPS, countDescendants("INTERRUPTED"));
    assertEquals(STEPS, launchStatistics("statistics$executions$total"));
    assertEquals(STEPS, launchStatistics("statistics$executions$failed"));
    assertEquals(STEPS, itemStatistics(ROOT_ID, "statistics$executions$total"));
  }

  @Test
  void finishParentItemFinishesAllInProgressDescendants() throws Exception {
    FinishTestItemRQ rq = new FinishTestItemRQ();
    rq.setLaunchUuid(LAUNCH_UUID);
    rq.setEndTime(Instant.now());

    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/" + ROOT_UUID)
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
    entityManager.flush();

    assertEquals(0, countItemsInProgress());
    assertEquals(STEPS, countDescendants("INTERRUPTED"));
    assertEquals(SUITES, countDescendants("FAILED"));
    assertEquals(STEPS, launchStatistics("statistics$executions$total"));
    assertEquals(STEPS, itemStatistics(ROOT_ID, "statistics$executions$total"));
  }

  private void insertItem(long id, String uuid, String type, boolean hasChildren, Long parentId,
      String path) {
    jdbcTemplate.update("""
            INSERT INTO test_item (item_id, uuid, name, type, start_time, last_modified, path, unique_id,
                                   test_case_hash, has_children, has_retries, has_stats, parent_id, launch_id)
            VALUES (?, ?, ?, CAST(? AS TEST_ITEM_TYPE_ENUM), now(), now(), CAST(? AS LTREE), ?, ?, ?, false,
                    true, ?, ?)
            """, id, uuid == null ? "finish-descendants-" + id : uuid, type + " " + id, type, path,
        "auto:finish-descendants-" + id, (int) id, hasChildren, parentId, LAUNCH_ID);
    jdbcTemplate.update(
        "INSERT INTO test_item_results (result_id, status) VALUES (?, CAST('IN_PROGRESS' AS STATUS_ENUM))",
        id);
  }

  private int countItemsInProgress() {
    return jdbcTemplate.queryForObject("""
        SELECT count(*) FROM test_item ti JOIN test_item_results r ON ti.item_id = r.result_id
        WHERE ti.launch_id = ? AND r.status = 'IN_PROGRESS'
        """, Integer.class, LAUNCH_ID);
  }

  private int countDescendants(String status) {
    return jdbcTemplate.queryForObject("""
        SELECT count(*) FROM test_item ti JOIN test_item_results r ON ti.item_id = r.result_id
        WHERE ti.launch_id = ? AND ti.item_id <> ? AND r.status = CAST(? AS STATUS_ENUM)
        """, Integer.class, LAUNCH_ID, ROOT_ID, status);
  }

  private int launchStatistics(String field) {
    return jdbcTemplate.queryForObject("""
        SELECT coalesce(sum(ls.s_counter), 0) FROM launch_statistics ls
        JOIN statistics_field sf ON ls.statistics_field_id = sf.sf_id
        WHERE ls.launch_id = ? AND sf.name = ?
        """, Integer.class, LAUNCH_ID, field);
  }

  private int itemStatistics(long itemId, String field) {
    return jdbcTemplate.queryForObject("""
        SELECT coalesce(sum(s.s_counter), 0) FROM statistics s
        JOIN statistics_field sf ON s.statistics_field_id = sf.sf_id
        WHERE s.item_id = ? AND sf.name = ?
        """, Integer.class, itemId, field);
  }
}
