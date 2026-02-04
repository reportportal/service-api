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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_BINARY_CONTENT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_TEST_ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_RETRY_PARENT_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLog;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Operator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author Ivan Budaev
 */
@Sql("/db/fill/item/items-fill.sql")
class LogRepositoryTest extends BaseMvcTest {

  @Autowired
  private TestItemRepository testItemRepository;

  @Autowired
  private LogRepository logRepository;

  @Test
  void updateLaunchIdByLaunchId() {

    final Filter firstLaunchFilter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(
            FilterCondition.builder().eq(CRITERIA_LOG_LAUNCH_ID, String.valueOf(1L)).build())
        .build();

    final Filter secondLaunchFilter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(
            FilterCondition.builder().eq(CRITERIA_LOG_LAUNCH_ID, String.valueOf(2L)).build())
        .build();

    final List<Long> firstLaunchLogIds = logRepository.findIdsByFilter(firstLaunchFilter);
    Assertions.assertFalse(firstLaunchLogIds.isEmpty());
    logRepository.updateLaunchIdByLaunchId(1L, 2L);

    final List<Long> secondLaunchLogIds = logRepository.findIdsByFilter(secondLaunchFilter);

    Assertions.assertFalse(secondLaunchLogIds.isEmpty());
    Assertions.assertTrue(secondLaunchLogIds.containsAll(firstLaunchLogIds));

    Assertions.assertTrue(logRepository.findIdsByFilter(firstLaunchFilter).isEmpty());
  }

  @Test
  void updateClusterIdByIds() {

    final List<Long> logIds = List.of(1L, 2L, 3L);

    final int updated = logRepository.updateClusterIdByIdIn(1L, logIds);

    assertEquals(3, updated);

    final List<Log> logs = logRepository.findAllById(logIds);

    logs.forEach(l -> assertEquals(1L, l.getClusterId()));
  }

  @Test
  void updateClusterIdSetNullByLaunchId() {

    final List<Long> logIds = List.of(1L, 2L, 3L);

    final int updated = logRepository.updateClusterIdByIdIn(1L, logIds);

    assertEquals(3, updated);

    final int nullUpdated = logRepository.updateClusterIdSetNullByLaunchId(1L);

    assertEquals(6, nullUpdated);

    final List<Log> logs = logRepository.findAllById(logIds);

    logs.forEach(l -> assertNull(l.getClusterId()));
  }

  @Test
  void updateClusterIdSetNullByItemIds() {

    final List<Long> logIds = List.of(1L, 2L, 3L);

    final int updated = logRepository.updateClusterIdByIdIn(1L, logIds);

    assertEquals(3, updated);

    final List<Long> itemIds = List.of(3L, 4L, 5L);
    final int nullUpdated = logRepository.updateClusterIdSetNullByItemIds(itemIds);

    assertEquals(6, nullUpdated);

    final List<Log> logs = logRepository.findAllById(logIds);

    logs.forEach(l -> assertNull(l.getClusterId()));
  }

  @Test
  void getPageNumberTest() {
    Filter filter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(new FilterCondition(Condition.EQUALS, false, "3", CRITERIA_TEST_ITEM_ID))
        .build();

    Integer number = logRepository.getPageNumber(1L, filter,
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, CRITERIA_LOG_TIME)));
    assertEquals(1L, (long) number, "Unexpected log page number");
  }

  @Test
  void hasLogsAddedLatelyTest() {
    assertTrue(
        logRepository.hasLogsAddedLately(Duration.ofDays(13).plusHours(23), 1L, StatusEnum.FAILED));
  }

  @Test
  void deleteByPeriodAndTestItemIdsTest() {
    int removedLogsCount = logRepository.deleteByPeriodAndTestItemIds(
        Duration.ofDays(13).plusHours(20), Collections.singleton(3L));
    assertEquals(3, removedLogsCount, "Incorrect count of deleted logs");
  }

  @Test
  void deleteByPeriodAndLaunchIdsTest() {
    int removedLogsCount = logRepository.deleteByPeriodAndLaunchIds(
        Duration.ofDays(13).plusHours(20), Collections.singleton(3L));
    assertEquals(1, removedLogsCount, "Incorrect count of deleted logs");
  }

  @Test
  void hasLogsTest() {
    assertTrue(logRepository.hasLogs(3L));
    assertFalse(logRepository.hasLogs(100L));
  }

  @Test
  void findByTestItemIdWithLimitTest() {
    final long itemId = 3L;

    final List<Log> logs = logRepository.findByTestItemId(itemId, 2);

    assertNotNull(logs, "Logs should not be null");
    assertEquals(2, logs.size(), "Unexpected logs count");
    logs.forEach(it -> assertEquals(itemId, (long) it.getTestItem().getItemId(),
        "Log has incorrect item id"));
  }

  @Test
  void findByTestItemId() {
    final Long itemId = 3L;

    final List<Log> logs = logRepository.findByTestItemId(itemId);

    assertNotNull(logs, "Logs should not be null");
    assertTrue(!logs.isEmpty(), "Logs should not be empty");
    logs.forEach(
        it -> assertEquals(itemId, it.getTestItem().getItemId(), "Log has incorrect item id"));
  }

  @Test
  void findIdsByTestItemId() {
    final long itemId = 3L;

    final List<Long> logIds = logRepository.findIdsByTestItemId(itemId);

    assertNotNull(logIds, "Log ids should not be null");
    assertTrue(!logIds.isEmpty(), "Log ids should not be empty");
    assertEquals(7, logIds.size());
  }

  @Test
  void findItemLogIdsByLaunchId() {
    List<Long> logIdsByLaunch = logRepository.findItemLogIdsByLaunchIdAndLogLevelGte(1L,
        LogLevel.DEBUG.toInt());
    assertEquals(7, logIdsByLaunch.size());
  }

  @Test
  void findItemLogIdsByLaunchIds() {
    List<Long> logIds = logRepository.findItemLogIdsByLaunchIdsAndLogLevelGte(Arrays.asList(1L, 2L),
        LogLevel.DEBUG.toInt());
    assertEquals(7, logIds.size());
  }

  @Test
  void findIdsByItemIds() {
    List<Long> errorIds = logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
        1L,
        Arrays.asList(1L, 2L, 3L),
        LogLevel.DEBUG.toInt()
    );
    List<Log> errorLogs = logRepository.findAllById(errorIds);
    errorLogs.forEach(log -> assertEquals(40000, log.getLogLevel()));
    assertEquals(7, errorIds.size());
    assertEquals(7, errorLogs.size());

    List<Long> ids = logRepository.findIdsByTestItemIdsAndLogLevelGte(Arrays.asList(1L, 2L, 3L),
        LogLevel.FATAL.toInt());
    assertEquals(0, ids.size());
  }

  @Test
  void findIdsByItemIdsAndLogLevelGte() {
    List<Long> errorIds = logRepository.findIdsByTestItemIdsAndLogLevelGte(
        Arrays.asList(1L, 2L, 3L), LogLevel.DEBUG.toInt());
    List<Log> errorLogs = logRepository.findAllById(errorIds);
    errorLogs.forEach(log -> assertEquals(40000, log.getLogLevel()));
    assertEquals(7, errorIds.size());
    assertEquals(7, errorLogs.size());

    List<Long> ids = logRepository.findIdsByTestItemIdsAndLogLevelGte(Arrays.asList(1L, 2L, 3L),
        LogLevel.FATAL.toInt());
    assertEquals(0, ids.size());
  }

  @Test
  void findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte() {

    int logLevel = LogLevel.WARN_INT;

    List<Long> itemIds = Arrays.asList(1L, 2L, 3L);
    List<Log> logs = logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(1L,
        itemIds, logLevel);

    assertTrue(logs != null && logs.size() != 0, "Logs should be not null or empty");
    logs.forEach(log -> {
      Long itemId = log.getTestItem().getItemId();
      assertNotNull(itemId);
      assertTrue(itemIds.contains(itemId), "Incorrect item id");
      assertTrue(log.getLogLevel() >= logLevel, "Unexpected log level");
    });
  }

  @Test
  void findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte() {
    int logLevel = LogLevel.WARN_INT;

    final Set<Long> logsWithCluster = Set.of(4L, 5L, 6L);

    List<Long> itemIds = Arrays.asList(1L, 2L, 3L);
    final Map<Long, List<IndexLog>> logMapping = logRepository.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
        1L,
        itemIds,
        logLevel
    );

    assertFalse(logMapping.isEmpty(), "Logs should be not empty");
    logMapping.forEach((itemId, logs) -> {
      assertNotNull(itemId);
      assertTrue(itemIds.contains(itemId), "Incorrect item id");
      logs.forEach(logIndex -> {
        if (logsWithCluster.contains(logIndex.getLogId())) {
          assertNotNull(logIndex.getClusterId());
        }
        assertTrue(logIndex.getLogLevel() >= logLevel, "Unexpected log level");
      });
    });
  }

  @Test
  void findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte() {

    int logLevel = LogLevel.WARN_INT;

    Long itemId = 1L;
    List<Log> logs = logRepository.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(1L,
        itemId, logLevel, 3);

    assertTrue(logs != null && logs.size() == 3, "Logs should be not null or empty");
    logs.forEach(log -> {
      Long id = log.getTestItem().getItemId();
      assertNotNull(id);
      assertEquals(itemId, id, "Incorrect item id");
      assertTrue(log.getLogLevel() >= logLevel, "Unexpected log level");
    });
  }

  @Test
  void findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit() {

    List<Long> itemIds = Arrays.asList(1L, 2L, 3L);
    List<Log> logs = logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(1L,
        itemIds, 4);

    assertTrue(logs != null && logs.size() != 0, "Logs should be not null or empty");
    assertEquals(4, logs.size());
    logs.forEach(log -> {
      Long itemId = log.getTestItem().getItemId();
      assertNotNull(itemId);
      assertNotNull(log.getAttachment());
      assertTrue(itemIds.contains(itemId), "Incorrect item id");
    });
  }

  @Test
  void findNestedItemsTest() {

    Filter filter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(new FilterCondition(Condition.EQUALS, false, "2", CRITERIA_TEST_ITEM_ID))
        .withCondition(new FilterCondition(Condition.IN, false, "FAILED, PASSED", CRITERIA_STATUS))
        .build();

    logRepository.findNestedItems(2L, false, false, filter, PageRequest.of(2, 1));
  }

  @Test
  void findIdsByFilter() {

    Filter failedStatusFilter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(FilterCondition.builder().eq(CRITERIA_STATUS, "FAILED").build())
        .build();

    List<Long> ids = logRepository.findIdsByFilter(failedStatusFilter);

    assertEquals(7, ids.size());
  }

  @Test
  void findAllWithAttachment() {
    Filter logWithAttachmentsFilter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(FilterCondition.builder()
            .withCondition(Condition.EXISTS)
            .withSearchCriteria(CRITERIA_LOG_BINARY_CONTENT)
            .withValue("1")
            .build())
        .build();

    Page<Log> logPage = logRepository.findByFilter(logWithAttachmentsFilter, PageRequest.of(0, 10));

    List<Log> logs = logPage.getContent();
    assertFalse(logs.isEmpty());

    logs.forEach(log -> {
      Attachment attachment = log.getAttachment();
      assertNotNull(attachment);
      assertNotNull(attachment.getId());
      assertNotNull(attachment.getFileId());
      assertNotNull(attachment.getContentType());
      assertNotNull(attachment.getThumbnailId());
    });

    assertEquals(10, logs.size());
  }

  @Test
  void findAllWithAttachmentOfRetries() {

    Filter logWithAttachmentsFilter = Filter.builder()
        .withTarget(Log.class)
        .withCondition(FilterCondition.builder()
            .withCondition(Condition.EXISTS)
            .withSearchCriteria(CRITERIA_LOG_BINARY_CONTENT)
            .withValue("1")
            .build())
        .withCondition(new CompositeFilterCondition(Lists.newArrayList(FilterCondition.builder()
                .eq(CRITERIA_RETRY_PARENT_LAUNCH_ID, String.valueOf(1L))
                .build(),
            FilterCondition.builder().eq(CRITERIA_ITEM_LAUNCH_ID, String.valueOf(1L))
                .withOperator(Operator.OR).build()
        )))
        .build();

    Page<Log> logPage = logRepository.findByFilter(logWithAttachmentsFilter, PageRequest.of(0, 10));

    List<Log> logs = logPage.getContent();
    assertFalse(logs.isEmpty());

    logs.forEach(log -> {
      Attachment attachment = log.getAttachment();
      assertNotNull(attachment);
      assertNotNull(attachment.getId());
      assertNotNull(attachment.getFileId());
      assertNotNull(attachment.getContentType());
      assertNotNull(attachment.getThumbnailId());
    });

    assertEquals(7, logs.size());
  }

  @Sql("/db/fill/item/items-with-nested-steps.sql")
  @Test
  void findLogMessagesByItemIdAndLogLevelNestedTest() {

    TestItem testItem = testItemRepository.findById(132L).get();

    List<String> messagesByItemIdAndLevelGte = logRepository.findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(
        testItem.getLaunchId(),
        testItem.getItemId(),
        testItem.getPath(),
        LogLevel.ERROR.toInt()
    );
    assertTrue(CollectionUtils.isNotEmpty(messagesByItemIdAndLevelGte));
    assertEquals(1, messagesByItemIdAndLevelGte.size());
    assertEquals("java.lang.NullPointerException: Oops\n"
            + "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
            + "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
            + "\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
            + "\tat java.base/java.lang.reflect.Method.invoke(Method.java:566)\n",
        messagesByItemIdAndLevelGte.get(0));
  }

  @Test
  void findLogMessagesByItemIdAndLoLevelGteTest() {

    TestItem testItem = testItemRepository.findById(3L).get();

    List<String> messagesByItemIdAndLevelGte = logRepository.findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(
        testItem.getLaunchId(),
        testItem.getItemId(),
        testItem.getPath(),
        LogLevel.WARN.toInt()
    );
    assertTrue(CollectionUtils.isNotEmpty(messagesByItemIdAndLevelGte));
    assertEquals(7, messagesByItemIdAndLevelGte.size());
    messagesByItemIdAndLevelGte.forEach(it -> assertEquals("log", it));
  }
}
