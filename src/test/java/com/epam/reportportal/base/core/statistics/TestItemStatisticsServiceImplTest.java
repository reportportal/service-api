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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.statistics.repository.StatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.StatisticsFieldRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TestItemStatisticsServiceImplTest {

  private static final Map<String, Long> FIELD_IDS = Map.of(
      "statistics$executions$total", 1L,
      "statistics$executions$passed", 2L,
      "statistics$defects$product_bug$total", 8L,
      "statistics$defects$product_bug$pb001", 9L,
      "statistics$defects$product_bug$pb_custom", 42L);

  private final StatisticsRepository statisticsRepository = mock(StatisticsRepository.class);
  private final StatisticsFieldRepository statisticsFieldRepository = mock(
      StatisticsFieldRepository.class);
  private final TestItemStatisticsServiceImpl service = new TestItemStatisticsServiceImpl(
      statisticsRepository, statisticsFieldRepository);

  @BeforeEach
  void setUp() {
    when(statisticsRepository.hasStatistics(any())).thenReturn(false);
    when(statisticsRepository.canHaveExecutionStats(any())).thenReturn(true);
    when(statisticsRepository.canHaveIssueStats(any())).thenReturn(true);
    when(statisticsFieldRepository.findByName(anyString())).thenAnswer(invocation -> {
      String name = invocation.getArgument(0);
      return Optional.ofNullable(FIELD_IDS.get(name)).map(id -> field(name, id));
    });
  }

  @AfterEach
  void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void predefinedFieldsAreLookedUpOnlyOnce() {
    service.addStatistics(item(1L, null));
    service.addStatistics(item(2L, null));
    service.addStatistics(item(3L, null));

    verify(statisticsFieldRepository, times(1)).findByName("statistics$executions$total");
    verify(statisticsFieldRepository, times(1)).findByName("statistics$executions$passed");
    ArgumentCaptor<Long[]> fieldIds = ArgumentCaptor.forClass(Long[].class);
    verify(statisticsRepository, times(3)).incrementForLaunch(any(), fieldIds.capture());
    fieldIds.getAllValues().forEach(ids -> assertArrayEquals(new Long[]{2L, 1L}, ids));
  }

  @Test
  void customSubTypeFieldIsNotCached() {
    service.addStatistics(item(1L, issueType("pb_custom")));
    service.addStatistics(item(2L, issueType("pb_custom")));

    verify(statisticsFieldRepository, times(2)).findByName(
        "statistics$defects$product_bug$pb_custom");
    verify(statisticsFieldRepository, times(1)).findByName("statistics$defects$product_bug$total");
  }

  @Test
  void fieldIsCachedOnlyAfterCommit() {
    TransactionSynchronizationManager.initSynchronization();
    service.addStatistics(item(1L, null));
    service.addStatistics(item(2L, null));
    // rolled back: the synchronizations are discarded without afterCommit
    TransactionSynchronizationManager.clearSynchronization();

    TransactionSynchronizationManager.initSynchronization();
    service.addStatistics(item(3L, null));
    verify(statisticsFieldRepository, times(2)).findByName("statistics$executions$total");
    TransactionSynchronizationManager.getSynchronizations()
        .forEach(TransactionSynchronization::afterCommit);
    TransactionSynchronizationManager.clearSynchronization();

    service.addStatistics(item(4L, null));
    verify(statisticsFieldRepository, times(2)).findByName("statistics$executions$total");
  }

  @Test
  void coldCacheRegistersOneSynchronizationPerTransaction() {
    TransactionSynchronizationManager.initSynchronization();
    service.addStatistics(item(1L, issueType("pb001")));
    service.addStatistics(item(2L, issueType("pb001")));
    service.addStatistics(item(3L, issueType("pb001")));

    assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
    verify(statisticsFieldRepository, times(1)).findByName("statistics$executions$total");
    verify(statisticsFieldRepository, times(1)).findByName("statistics$executions$passed");
    verify(statisticsFieldRepository, times(1)).findByName("statistics$defects$product_bug$total");
    verify(statisticsFieldRepository, times(1)).findByName("statistics$defects$product_bug$pb001");
  }

  private static StatisticsField field(String name, Long id) {
    StatisticsField field = new StatisticsField(name);
    field.setId(id);
    return field;
  }

  private static IssueType issueType(String locator) {
    IssueGroup group = new IssueGroup();
    group.setTestItemIssueGroup(TestItemIssueGroup.PRODUCT_BUG);
    IssueType issueType = new IssueType();
    issueType.setLocator(locator);
    issueType.setIssueGroup(group);
    return issueType;
  }

  private static TestItem item(Long id, IssueType issueType) {
    TestItem item = new TestItem();
    item.setItemId(id);
    item.setLaunchId(10L);
    item.setPath("100." + id);
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.PASSED);
    if (issueType != null) {
      IssueEntity issue = new IssueEntity();
      issue.setIssueType(issueType);
      results.setIssue(issue);
    }
    item.setItemResults(results);
    return item;
  }
}
