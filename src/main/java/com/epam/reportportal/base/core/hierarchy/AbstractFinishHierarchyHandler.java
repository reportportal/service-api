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

package com.epam.reportportal.base.core.hierarchy;

import static com.epam.reportportal.base.core.item.impl.status.ToSkippedStatusChangingStrategy.SKIPPED_ISSUE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.FAILED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.PASSED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.SKIPPED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum.SUITE;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.core.item.impl.retry.RetryHandler;
import com.epam.reportportal.base.core.item.impl.status.ChangeStatusHandler;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Abstract base handler for finishing a hierarchy of test items or launches.
 *
 * @param <T> the finish request type
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractFinishHierarchyHandler<T> implements FinishHierarchyHandler<T> {

  public static final int ITEM_PAGE_SIZE = 50;

  public static final String ATTRIBUTE_KEY_STATUS = "status";
  public static final String ATTRIBUTE_VALUE_INTERRUPTED = "interrupted";

  protected final LaunchRepository launchRepository;
  protected final TestItemRepository testItemRepository;
  protected final LaunchAttributeRepository launchAttributeRepository;
  protected final IssueEntityRepository issueEntityRepository;
  private final RetryHandler retryHandler;
  private final IssueTypeHandler issueTypeHandler;
  private final ChangeStatusHandler changeStatusHandler;
  private final TestItemStatisticsService statisticsService;

  /**
   * Wires repositories and services used to finish child items.
   *
   * @param launchRepository        launch persistence
   * @param testItemRepository      test item persistence
   * @param issueEntityRepository   issue / defect link persistence
   * @param retryHandler            retry propagation helper
   * @param issueTypeHandler        project issue type resolution
   * @param changeStatusHandler     status transition service
   */
  public AbstractFinishHierarchyHandler(LaunchRepository launchRepository,
      TestItemRepository testItemRepository,
      IssueEntityRepository issueEntityRepository,
      LaunchAttributeRepository launchAttributeRepository,
      RetryHandler retryHandler,
      IssueTypeHandler issueTypeHandler,
      ChangeStatusHandler changeStatusHandler,
      TestItemStatisticsService statisticsService) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.launchAttributeRepository = launchAttributeRepository;
    this.issueEntityRepository = issueEntityRepository;
    this.retryHandler = retryHandler;
    this.issueTypeHandler = issueTypeHandler;
    this.changeStatusHandler = changeStatusHandler;
    this.statisticsService = statisticsService;
  }

  /**
   * Returns whether a defect type must be assigned for the target status and parent.
   *
   * @param status target status for children
   * @param entity parent (launch or item) context
   * @return whether an issue type must be assigned when moving to the given status
   */
  protected abstract boolean isIssueRequired(StatusEnum status, T entity);

  /**
   * Loads the ids of all {@link StatusEnum#IN_PROGRESS} descendants of the parent in one query. Items without children
   * are ordered by id, items with children deepest first.
   *
   * @param hasChildren whether to load the descendants that have children
   * @param entity      parent (launch or item) context
   * @return ids of the descendants to finish
   */
  protected abstract List<Long> getItemIds(boolean hasChildren, T entity);

  /**
   * Returns the id of the launch the descendants belong to.
   *
   * @param entity parent (launch or item) context
   * @return launch id, may be {@code null}
   */
  protected abstract Long getLaunchId(T entity);

  /**
   * Whether a skipped run should be treated as interrupted for the given launch (from attributes).
   *
   * @param status   target child status
   * @param launchId launch to read attributes for
   * @return true if interrupted should apply
   */
  protected boolean evaluateSkippedAttributeValue(StatusEnum status, Long launchId) {
    if (SKIPPED.equals(status)) {
      return launchAttributeRepository.findByLaunchIdAndKeyAndSystem(launchId, SKIPPED_ISSUE_KEY,
              true)
          .map(attribute -> BooleanUtils.toBoolean(attribute.getValue()))
          .orElse(false);
    } else {
      return false;
    }
  }

  /**
   * Resolves an issue type when required.
   *
   * @param isIssueRequired when true, resolve the type
   * @param projectId       project scope
   * @param locator         issue type locator
   * @return issue type if required
   */
  protected Optional<IssueType> getIssueType(boolean isIssueRequired, Long projectId,
      String locator) {
    if (isIssueRequired) {
      return Optional.of(issueTypeHandler.defineIssueType(projectId, locator));
    }
    return Optional.empty();
  }

  @Override
  public int finishDescendants(T parentEntity, StatusEnum status, Instant endDate,
      ReportPortalUser user,
      MembershipDetails membershipDetails) {

    expect(status, s -> s != IN_PROGRESS).verify(INCORRECT_REQUEST,
        "Unable to update current status to - " + IN_PROGRESS);

    // Lock before reading the ids so that no finish of the same launch commits between the snapshot and the updates
    ofNullable(getLaunchId(parentEntity)).ifPresent(statisticsService::acquireAdvisoryLock);

    final int withoutChildren = updateDescendantsWithoutChildren(parentEntity,
        membershipDetails, status, endDate, user);
    final int withChildren = updateDescendantsWithChildren(parentEntity, endDate);
    return withoutChildren + withChildren;
  }

  private int updateDescendantsWithoutChildren(T entity, MembershipDetails membershipDetails, StatusEnum status,
      Instant endTime, ReportPortalUser user) {
    Long projectId = membershipDetails.getProjectId();
    AtomicInteger updatedCount = new AtomicInteger(0);
    getIssueType(isIssueRequired(status, entity),
        projectId,
        TO_INVESTIGATE.getLocator()
    ).ifPresentOrElse(issueType -> processInChunks(getItemIds(false, entity),
            itemIdsWithoutChildrenHandler(issueType, status, endTime, membershipDetails, user, updatedCount)
        ),
        () -> processInChunks(getItemIds(false, entity),
            itemIdsWithoutChildrenHandler(status, endTime, membershipDetails, user, updatedCount)
        )
    );
    // The parent pass reads the final statuses of the children from the database
    testItemRepository.flush();
    return updatedCount.get();
  }

  private Consumer<List<Long>> itemIdsWithoutChildrenHandler(IssueType issueType, StatusEnum status,
      Instant endTime,
      MembershipDetails membershipDetails, ReportPortalUser user, AtomicInteger updatedCount) {
    return itemIds -> {
      Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
      itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).filter(this::isInProgress)
          .ifPresent(testItem -> {
            finishItem(testItem, status, endTime);
            attachIssue(testItem, issueType);
            statisticsService.addStatistics(testItem);
            changeStatusHandler.changeParentStatus(testItem, membershipDetails, user);
            updatedCount.incrementAndGet();
          }));
    };
  }

  private Consumer<List<Long>> itemIdsWithoutChildrenHandler(StatusEnum status,
      Instant endTime, MembershipDetails membershipDetails,
      ReportPortalUser user, AtomicInteger updatedCount) {
    return itemIds -> {
      Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
      itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).filter(this::isInProgress)
          .ifPresent(testItem -> {
            finishItem(testItem, status, endTime);
            statisticsService.addStatistics(testItem);
            changeStatusHandler.changeParentStatus(testItem, membershipDetails, user);
            updatedCount.incrementAndGet();
          }));
    };
  }

  /**
   * Attach default issue to the item only if it wasn't already created
   *
   * @param testItem  {@link TestItem}
   * @param issueType {@link IssueType}
   */
  private void attachIssue(TestItem testItem, IssueType issueType) {
    if (!SUITE.sameLevel(testItem.getType()) && testItem.isHasStats()) {
      issueEntityRepository.findById(testItem.getItemId()).ifPresentOrElse(issue -> {
      }, () -> {
        IssueEntity issueEntity = new IssueEntity();
        issueEntity.setIssueType(issueType);
        issueEntity.setTestItemResults(testItem.getItemResults());
        issueEntityRepository.save(issueEntity);
        testItem.getItemResults().setIssue(issueEntity);
      });
    }
  }

  private int updateDescendantsWithChildren(T entity, Instant endTime) {
    AtomicInteger updatedCount = new AtomicInteger(0);
    processInChunks(getItemIds(true, entity), itemIdsWithChildrenHandler(endTime, updatedCount));
    return updatedCount.get();
  }

  /**
   * Processes the ids collected once in chunks of {@link #ITEM_PAGE_SIZE}. The ids must not be re-queried page by page
   * with an offset: every processed item leaves the queried status, so an offset would skip unprocessed items.
   */
  private void processInChunks(List<Long> itemIds, Consumer<List<Long>> handler) {
    Lists.partition(itemIds, ITEM_PAGE_SIZE).forEach(handler);
  }

  private Consumer<List<Long>> itemIdsWithChildrenHandler(Instant endTime,
      AtomicInteger updatedCount) {
    return itemIds -> {
      Map<Long, TestItem> itemMapping = getItemMapping(itemIds);
      itemIds.forEach(itemId -> ofNullable(itemMapping.get(itemId)).filter(this::isInProgress)
          .ifPresent(testItem -> {
            boolean isFailed = testItemRepository.hasDescendantsNotInStatus(testItem.getItemId(),
                StatusEnum.PASSED.name(),
                StatusEnum.INFO.name(),
                StatusEnum.WARN.name()
            );
            finishItem(testItem, isFailed ? FAILED : PASSED, endTime);
            updatedCount.incrementAndGet();
          }));
    };
  }

  private Map<Long, TestItem> getItemMapping(List<Long> itemIds) {
    return testItemRepository.findAllById(itemIds).stream()
        .collect(Collectors.toMap(TestItem::getItemId, i -> i));
  }

  private boolean isInProgress(TestItem testItem) {
    return IN_PROGRESS == testItem.getItemResults().getStatus();
  }

  private void finishItem(TestItem testItem, StatusEnum status, Instant endTime) {
    testItem.getItemResults().setStatus(status);
    testItem.getItemResults().setEndTime(endTime);
    ItemAttribute interruptedAttribute = new ItemAttribute(ATTRIBUTE_KEY_STATUS,
        ATTRIBUTE_VALUE_INTERRUPTED, false);
    interruptedAttribute.setTestItem(testItem);
    testItem.getAttributes().add(interruptedAttribute);
    if (testItem.isHasRetries()) {
      retryHandler.finishRetries(testItem, JStatusEnum.valueOf(status.name()), endTime);
    }
  }
}
