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

package com.epam.reportportal.base.core.statistics;

import com.epam.reportportal.base.core.item.repository.TestItemPathContext;
import com.epam.reportportal.base.core.statistics.repository.StatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.StatisticsFieldRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * JPA-based implementation of {@link TestItemStatisticsService}.
 * <p>
 * All methods acquire a PostgreSQL advisory lock ({@code pg_advisory_xact_lock}) on the launch ID
 * to serialize statistics operations within the same launch, preventing deadlocks.
 * <p>
 * Uses JPA repositories for all database operations.
 *
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class
TestItemStatisticsServiceImpl implements TestItemStatisticsService {

  private static final String EXECUTIONS_TOTAL = "statistics$executions$total";
  private static final String EXECUTIONS_PREFIX = "statistics$executions$";
  private static final String DEFECTS_PREFIX = "statistics$defects$";

  /**
   * The predefined fields created by the schema migration, which are never deleted: total/passed/failed/skipped
   * executions and, per defect group, the total and the default sub-type. Other fields are not cached: custom sub-type
   * fields are deleted with the sub-type, and the demo data flush deletes every non-predefined field.
   */
  private static final Set<String> PREDEFINED_FIELDS = Stream.concat(
      Stream.of(EXECUTIONS_TOTAL, EXECUTIONS_PREFIX + "passed", EXECUTIONS_PREFIX + "failed",
          EXECUTIONS_PREFIX + "skipped"),
      Arrays.stream(TestItemIssueGroup.values())
          .filter(group -> group != TestItemIssueGroup.NOT_ISSUE_FLAG)
          .flatMap(group -> Stream.of(
              DEFECTS_PREFIX + group.getValue().toLowerCase() + "$total",
              DEFECTS_PREFIX + group.getValue().toLowerCase() + "$" + group.getLocator())))
      .collect(Collectors.toUnmodifiableSet());

  private final StatisticsRepository statisticsRepository;
  private final StatisticsFieldRepository statisticsFieldRepository;

  /**
   * Ids of predefined statistics fields by name. Filled only after the transaction that read or created the field has
   * committed, so a rolled back insert is never cached.
   */
  private final Map<String, Long> predefinedFieldIds = new ConcurrentHashMap<>();


  @Override
  @Transactional
  public void addStatistics(TestItem item) {
    if (item.getLaunchId() == null || item.getPath() == null) {
      return;
    }
    Long launchId = item.getLaunchId();
    StatusEnum status = item.getItemResults().getStatus();
    Long[] pathIds = parsePathIds(item.getPath());

    acquireAdvisoryLock(launchId);

    if (statisticsRepository.hasStatistics(item.getItemId())) {
      return;
    }

    if (statisticsRepository.canHaveExecutionStats(item.getItemId())) {
      String execField = executionFieldName(status);
      StatisticsField execFieldEntity = ensureStatisticsField(execField);
      StatisticsField totalFieldEntity = ensureStatisticsField(EXECUTIONS_TOTAL);

      incrementForAncestors(pathIds, execFieldEntity, totalFieldEntity);
      incrementForLaunch(launchId, execFieldEntity, totalFieldEntity);
    }

    IssueEntity issue = item.getItemResults().getIssue();
    if (issue != null && statisticsRepository.canHaveIssueStats(item.getItemId())) {
      StatisticsField fieldEntity = ensureStatisticsField(defectFieldName(issue.getIssueType()));
      StatisticsField totalEntity = ensureStatisticsField(
          defectTotalFieldName(issue.getIssueType()));

      incrementForAncestors(pathIds, fieldEntity, totalEntity);
      incrementForLaunch(launchId, fieldEntity, totalEntity);
    }

    statisticsRepository.flush();

  }

  @Override
  @Transactional
  public void addInterruptionStatistics(Long launchId) {
    if (launchId == null) {
      return;
    }

    acquireAdvisoryLock(launchId);
    List<TestItemPathContext> items = statisticsRepository.selectInterruptedItems(launchId);
    StatusEnum status = StatusEnum.FAILED;

    for (var item : items) {
      if (item.getPath() == null) {
        continue;
      }
      Long[] pathIds = parsePathIds(item.getPath());
      if (statisticsRepository.canHaveExecutionStats(item.getItemId())) {
        String execField = executionFieldName(status);
        StatisticsField execFieldEntity = ensureStatisticsField(execField);
        StatisticsField totalFieldEntity = ensureStatisticsField(EXECUTIONS_TOTAL);

        incrementForAncestors(pathIds, execFieldEntity, totalFieldEntity);
        incrementForLaunch(launchId, execFieldEntity, totalFieldEntity);
      }
    }

    statisticsRepository.flush();

  }

  @Override
  @Transactional
  public void deleteItemStatistics(TestItemPathContext item) {
    if (item.getLaunchId() == null || item.getPath() == null) {
      return;
    }
    Long launchId = item.getLaunchId();
    Long itemId = item.getItemId();
    Long[] pathIds = parsePathIds(item.getPath());

    acquireAdvisoryLock(launchId);

    // Subtract item's non-zero counters from all ancestors (excluding self)
    statisticsRepository.subtractItemStatsFromAncestors(itemId, pathIds);
    statisticsRepository.subtractItemStatsFromLaunch(itemId, launchId);
    statisticsRepository.deleteByItemId(itemId);

    statisticsRepository.flush();
  }


  @Override
  @Transactional
  public void changeDefectStatistics(TestItem item, IssueType oldIssueType,
      IssueType newIssueType) {
    if (item.getLaunchId() == null || item.getPath() == null) {
      return;
    }
    Long launchId = item.getLaunchId();
    Long[] pathIds = parsePathIds(item.getPath());

    acquireAdvisoryLock(launchId);

    // ── Decrease old defect ──
    if (oldIssueType != null) {
      StatisticsField oldFieldEntity = resolveStatisticsField(defectFieldName(oldIssueType));
      StatisticsField oldTotalEntity = resolveStatisticsField(defectTotalFieldName(oldIssueType));
      if (oldFieldEntity != null && oldTotalEntity != null) {
        decrementForAncestors(pathIds, 1, oldFieldEntity, oldTotalEntity);
        decrementForLaunch(launchId, 1, oldFieldEntity, oldTotalEntity);
      }
    }

    // ── Increase new defect ──
    if (newIssueType != null) {
      StatisticsField newFieldEntity = ensureStatisticsField(defectFieldName(newIssueType));
      StatisticsField newTotalEntity = ensureStatisticsField(defectTotalFieldName(newIssueType));
      incrementForAncestors(pathIds, newFieldEntity, newTotalEntity);
      incrementForLaunch(launchId, newFieldEntity, newTotalEntity);
    }
  }


  /**
   * Acquires a transaction-scoped advisory lock on the launch ID. Serializes all statistics
   * operations within the same launch, preventing deadlocks.
   */
  public void acquireAdvisoryLock(Long launchId) {
    statisticsRepository.performAdvisoryLock(launchId);
  }

  /**
   * Parses the ltree path string (e.g. "1.2.3") into an array of item IDs.
   */
  private Long[] parsePathIds(String path) {
    return Arrays.stream(path.split("\\."))
        .map(Long::parseLong)
        .toArray(Long[]::new);
  }

  /**
   * Ensures the statistics field exists and returns it.
   */
  private StatisticsField ensureStatisticsField(String name) {
    StatisticsField cached = getCachedStatisticsField(name);
    if (cached != null) {
      return cached;
    }
    StatisticsField field = statisticsFieldRepository.findByName(name)
        .orElseGet(() -> statisticsFieldRepository.save(new StatisticsField(name)));
    cacheStatisticsField(field);
    return field;
  }

  /**
   * Resolves the statistics field by name. Returns null if not found.
   */
  private StatisticsField resolveStatisticsField(String name) {
    StatisticsField cached = getCachedStatisticsField(name);
    if (cached != null) {
      return cached;
    }
    Optional<StatisticsField> field = statisticsFieldRepository.findByName(name);
    field.ifPresent(this::cacheStatisticsField);
    return field.orElse(null);
  }

  private StatisticsField getCachedStatisticsField(String name) {
    Long id = predefinedFieldIds.get(name);
    if (id == null && PREDEFINED_FIELDS.contains(name)) {
      id = Optional.ofNullable(pendingFieldIds(false)).map(pending -> pending.ids.get(name)).orElse(null);
    }
    if (id == null) {
      return null;
    }
    StatisticsField field = new StatisticsField(name);
    field.setId(id);
    return field;
  }

  private void cacheStatisticsField(StatisticsField field) {
    if (!PREDEFINED_FIELDS.contains(field.getName()) || field.getId() == null) {
      return;
    }
    String name = field.getName();
    Long id = field.getId();
    PendingFieldIds pending = pendingFieldIds(true);
    if (pending != null) {
      pending.ids.put(name, id);
    } else {
      predefinedFieldIds.put(name, id);
    }
  }

  /**
   * Returns the ids resolved by the current transaction and not yet committed, registering one synchronization per
   * transaction on demand. Returns null outside a transaction. Looked up among the current synchronizations, so a
   * suspended outer transaction's pending ids are not visible to a nested one.
   */
  private PendingFieldIds pendingFieldIds(boolean create) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      return null;
    }
    for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
      if (synchronization instanceof PendingFieldIds pending && pending.target == predefinedFieldIds) {
        return pending;
      }
    }
    if (!create) {
      return null;
    }
    PendingFieldIds pending = new PendingFieldIds(predefinedFieldIds);
    TransactionSynchronizationManager.registerSynchronization(pending);
    return pending;
  }

  /**
   * Field ids resolved by one transaction, published to the cache only after it commits.
   */
  private static final class PendingFieldIds implements TransactionSynchronization {

    private final Map<String, Long> target;
    private final Map<String, Long> ids = new HashMap<>();

    private PendingFieldIds(Map<String, Long> target) {
      this.target = target;
    }

    @Override
    public void afterCommit() {
      target.putAll(ids);
    }
  }

  /**
   * Maps a {@link StatusEnum} to the execution statistics field name. INTERRUPTED is treated as
   * "failed" (same convention as the DB function).
   */
  private String executionFieldName(StatusEnum status) {
    return EXECUTIONS_PREFIX + status.getExecutionCounterField();
  }

  /**
   * Builds the defect statistics field name for a specific issue type. E.g.
   * {@code statistics$defects$product_bug$pb001}
   */
  private String defectFieldName(IssueType issueType) {
    String group = issueType.getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase();
    String locator = issueType.getLocator().toLowerCase();
    return DEFECTS_PREFIX + group + "$" + locator;
  }

  /**
   * Builds the defect total statistics field name for an issue type's group. E.g.
   * {@code statistics$defects$product_bug$total}
   */
  private String defectTotalFieldName(IssueType issueType) {
    String group = issueType.getIssueGroup().getTestItemIssueGroup().getValue().toLowerCase();
    return DEFECTS_PREFIX + group + "$total";
  }

  /**
   * Increments the specified statistics fields by 1 for all items in the path (ancestors + self).
   * Uses native SQL UPSERT for efficient bulk operations.
   */
  private void incrementForAncestors(Long[] pathIds, StatisticsField... fields) {
    Long[] fieldIds = Arrays.stream(fields).map(StatisticsField::getId).toArray(Long[]::new);
    statisticsRepository.incrementForAncestors(pathIds, fieldIds);
  }

  /**
   * Increments the specified statistics fields by 1 for the launch. Uses native SQL UPSERT for
   * efficient operations.
   */
  private void incrementForLaunch(Long launchId, StatisticsField... fields) {
    Long[] fieldIds = Arrays.stream(fields).map(StatisticsField::getId).toArray(Long[]::new);
    statisticsRepository.incrementForLaunch(launchId, fieldIds);
  }

  /**
   * Decrements the specified statistics fields by the given amount for all items in the path
   * (ancestors + self). Uses native SQL with GREATEST(0, ...) to prevent negative values.
   */
  private void decrementForAncestors(Long[] pathIds, int amount, StatisticsField... fields) {
    Long[] fieldIds = Arrays.stream(fields).map(StatisticsField::getId).toArray(Long[]::new);
    statisticsRepository.decrementForAncestors(pathIds, fieldIds, amount);
  }

  /**
   * Decrements the specified statistics fields by the given amount for the launch. Uses native SQL
   * with GREATEST(0, ...) to prevent negative values.
   */
  private void decrementForLaunch(Long launchId, int amount, StatisticsField... fields) {
    Long[] fieldIds = Arrays.stream(fields).map(StatisticsField::getId).toArray(Long[]::new);
    statisticsRepository.decrementForLaunch(launchId, fieldIds, amount);
  }
}
