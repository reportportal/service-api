/*
 * Copyright 2025 EPAM Systems
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.reporting.FinishTestItemRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * A v1 finish request of a launch with an item in progress waits up to the grace period
 * ({@code reporting.launch-finish.grace-period}, 3 s by default) for the finish request of the item that arrives
 * after it, without holding a lock that would block that request. The launch end time is the moment the request was
 * received, not the moment it was processed.
 *
 * <p>Data is committed (the requests run in their own transactions on different threads) and removed afterwards.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LaunchFinishGracePeriodTest extends BaseMvcTest {

  private static final long PROJECT_ID = 2L;
  private static final long USER_ID = 2L;
  private static final Duration LATE_CHILD_DELAY = Duration.ofMillis(700);

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JsonMapper objectMapper;

  private JdbcTemplate jdbc;
  private long launchId;
  private String launchUuid;
  private long itemId;
  private String itemUuid;

  @BeforeEach
  void createLaunchWithItemInProgress() {
    jdbc = new JdbcTemplate(dataSource);
    launchUuid = UUID.randomUUID().toString();
    launchId = jdbc.queryForObject("insert into launch (uuid, project_id, user_id, name, description, start_time, "
            + "number, last_modified, mode, status) values (?, ?, ?, ?, 'desc', now() - interval '1 minute', 1, now(), "
            + "'DEFAULT', 'IN_PROGRESS') returning id", Long.class, launchUuid, PROJECT_ID, USER_ID,
        "finish-grace-" + launchUuid);
    itemUuid = UUID.randomUUID().toString();
    itemId = jdbc.queryForObject("insert into test_item (uuid, name, type, start_time, last_modified, unique_id, "
            + "test_case_hash, has_children, has_retries, has_stats, launch_id) values (?, 'step', 'STEP', "
            + "now() - interval '1 minute', now(), ?, 1, false, false, true, ?) returning item_id", Long.class,
        itemUuid, "auto:finish-grace-" + itemUuid, launchId);
    jdbc.update("update test_item set path = cast(? as ltree) where item_id = ?", String.valueOf(itemId), itemId);
    jdbc.update("insert into test_item_results (result_id, status) values (?, 'IN_PROGRESS')", itemId);
  }

  @AfterEach
  void removeData() {
    jdbc.update("delete from activity where project_id = ? and object_id in (?, ?)", PROJECT_ID, launchId, itemId);
    // statistics, results and attributes are removed by cascade
    jdbc.update("delete from test_item where item_id = ?", itemId);
    jdbc.update("delete from launch where id = ?", launchId);
  }

  @Test
  void finishOfChildArrivingAfterLaunchFinishIsNotInterrupted() throws Exception {
    Instant sentAt = Instant.now();
    CompletableFuture<Duration> launchFinish = CompletableFuture.supplyAsync(() -> {
      try {
        mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/" + launchUuid + "/finish")
            .contentType(APPLICATION_JSON)
            .with(token(oAuthHelper.getDefaultToken()))
            // no end time: the launch ends at the moment the request was received
            .content("{}")).andExpect(status().isOk());
        return Duration.between(sentAt, Instant.now());
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    });

    Thread.sleep(LATE_CHILD_DELAY.toMillis());
    FinishTestItemRQ itemRq = new FinishTestItemRQ();
    itemRq.setLaunchUuid(launchUuid);
    itemRq.setStatus("passed");
    itemRq.setEndTime(Instant.now());
    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/" + itemUuid)
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content(objectMapper.writeValueAsBytes(itemRq))).andExpect(status().isOk());
    Instant childFinishedAt = Instant.now();

    Duration launchFinishTook = launchFinish.get(20, TimeUnit.SECONDS);

    assertEquals("PASSED", itemStatus());
    assertEquals("PASSED", launchStatus());
    assertTrue(launchFinishTook.compareTo(LATE_CHILD_DELAY) >= 0,
        "the launch finish should have waited for the child, took " + launchFinishTook);
    Instant launchEndTime = launchEndTime();
    assertTrue(!launchEndTime.isBefore(sentAt.minusMillis(1)) && launchEndTime.isBefore(sentAt.plus(LATE_CHILD_DELAY)),
        "launch end time " + launchEndTime + " should be the receive time, request sent at " + sentAt
            + ", child finished at " + childFinishedAt);
  }

  @Test
  void launchWithoutItemsInProgressIsFinishedAtOnce() throws Exception {
    jdbc.update("update test_item_results set status = 'PASSED', end_time = now() where result_id = ?", itemId);
    Instant endTime = Instant.now().minusSeconds(5).truncatedTo(ChronoUnit.MILLIS);

    Instant sentAt = Instant.now();
    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/" + launchUuid + "/finish")
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content("{\"endTime\":\"" + endTime + "\"}")).andExpect(status().isOk());
    Duration took = Duration.between(sentAt, Instant.now());

    assertTrue(took.compareTo(Duration.ofSeconds(2)) < 0, "finish without items in progress took " + took);
    assertEquals("PASSED", launchStatus());
    assertEquals(endTime.toEpochMilli(), launchEndTime().toEpochMilli(), "the end time of the request is kept");
  }

  @Test
  void launchWhoseItemStaysInProgressIsFinishedAfterTheGracePeriod() throws Exception {
    Instant sentAt = Instant.now();
    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/" + launchUuid + "/finish")
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content("{}")).andExpect(status().isOk());
    Duration took = Duration.between(sentAt, Instant.now());

    assertTrue(took.compareTo(Duration.ofMillis(2_900)) >= 0 && took.compareTo(Duration.ofSeconds(10)) < 0,
        "the finish should wait for the 3 s grace period, took " + took);
    assertEquals("INTERRUPTED", itemStatus());
    assertEquals("FAILED", launchStatus());
    Instant launchEndTime = launchEndTime();
    assertTrue(launchEndTime.isBefore(sentAt.plusSeconds(1)),
        "launch end time " + launchEndTime + " should be the receive time, not the processing time; sent at "
            + sentAt);
    assertEquals(launchEndTime.toEpochMilli(), itemEndTime().toEpochMilli(),
        "the interrupted item gets the end time of the launch");
  }

  @Test
  void itemFinishWithoutEndTimeIsStillRejected() throws Exception {
    mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/" + itemUuid)
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getDefaultToken()))
        .content("{\"launchUuid\":\"" + launchUuid + "\",\"status\":\"passed\"}"))
        .andExpect(status().isBadRequest());
    assertEquals("IN_PROGRESS", itemStatus());
  }

  private String itemStatus() {
    return jdbc.queryForObject("select status::text from test_item_results where result_id = ?", String.class,
        itemId);
  }

  private String launchStatus() {
    return jdbc.queryForObject("select status::text from launch where id = ?", String.class, launchId);
  }

  private Instant launchEndTime() {
    return jdbc.queryForObject("select end_time from launch where id = ?", Timestamp.class, launchId).toInstant();
  }

  private Instant itemEndTime() {
    return jdbc.queryForObject("select end_time from test_item_results where result_id = ?", Timestamp.class,
        itemId).toInstant();
  }
}
