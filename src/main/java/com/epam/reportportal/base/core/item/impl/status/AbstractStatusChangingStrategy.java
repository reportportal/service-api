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

package com.epam.reportportal.base.core.item.impl.status;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.FAILED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.INFO;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.PASSED;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.WARN;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.events.domain.item.TestItemStatusChangedEvent;
import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Default propagation rules for a target terminal status.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AbstractStatusChangingStrategy implements StatusChangingStrategy {

  protected final TestItemRepository testItemRepository;
  protected final IssueEntityRepository issueEntityRepository;
  protected final LogRepository logRepository;
  protected final LogIndexer logIndexer;
  private final TestItemService testItemService;
  private final ProjectRepository projectRepository;
  private final LaunchRepository launchRepository;
  private final IssueTypeHandler issueTypeHandler;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Assembles the strategy with repositories and support services.
   *
   * @param testItemService       domain service for items and launches
   * @param projectRepository     project access
   * @param launchRepository      launch access
   * @param testItemRepository    test item persistence
   * @param issueTypeHandler      issue type resolution
   * @param eventPublisher        for domain events
   * @param issueEntityRepository defect persistence
   * @param logRepository         log persistence
   * @param logIndexer            search index updates
   */
  protected AbstractStatusChangingStrategy(TestItemService testItemService,
      ProjectRepository projectRepository, LaunchRepository launchRepository,
      TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
      ApplicationEventPublisher eventPublisher, IssueEntityRepository issueEntityRepository,
      LogRepository logRepository, LogIndexer logIndexer) {
    this.testItemService = testItemService;
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.issueTypeHandler = issueTypeHandler;
    this.eventPublisher = eventPublisher;
    this.issueEntityRepository = issueEntityRepository;
    this.logRepository = logRepository;
    this.logIndexer = logIndexer;
  }

  /**
   * Persists the new status and any side effects (issues, parent updates, events).
   *
   * @param project        owning project
   * @param launch         launch the item belongs to
   * @param testItem       item whose status is being set
   * @param providedStatus new terminal (or other) status to apply
   * @param user           current user for activities and issue handling
   * @param updateParents  whether to propagate to ancestors
   */
  protected abstract void updateStatus(Project project, Launch launch, TestItem testItem,
      StatusEnum providedStatus, ReportPortalUser user, boolean updateParents);

  /**
   * Resolves the parent item's new aggregate status from its current and updated child.
   *
   * @param parentItem the ancestor whose status is derived
   * @param childItem  the descendant that was just updated
   * @return the status the parent should take
   */
  protected abstract StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem);

  @Override
  public void changeStatus(TestItem testItem, StatusEnum providedStatus, ReportPortalUser user,
      boolean updateParents) {
    BusinessRule.expect(testItem.getItemResults().getStatus(),
        currentStatus -> !IN_PROGRESS.equals(currentStatus)
    ).verify(
        INCORRECT_REQUEST, Suppliers.formattedSupplier(
            "Unable to update status of test item = '{}' because of '{}' status",
            testItem.getItemId(), testItem.getItemResults().getStatus()
        ).get());
    if (providedStatus == testItem.getItemResults().getStatus()) {
      return;
    }

    Launch launch = testItemService.getEffectiveLaunch(testItem);
    Project project = projectRepository.findById(launch.getProjectId())
        .orElseThrow(
            () -> new ReportPortalException(NOT_FOUND, "Project " + launch.getProjectId()));

    updateStatus(project, launch, testItem, providedStatus, user, updateParents);
  }

  /**
   * Attaches a "To Investigate" issue when a terminal status requires it.
   *
   * @param testItem  item receiving the defect
   * @param projectId project id for issue type resolution
   */
  protected void addToInvestigateIssue(TestItem testItem, Long projectId) {
    IssueEntity issueEntity = new IssueEntity();
    IssueType toInvestigate =
        issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
    issueEntity.setIssueType(toInvestigate);
    issueEntity.setTestItemResults(testItem.getItemResults());
    issueEntityRepository.save(issueEntity);
    testItem.getItemResults().setIssue(issueEntity);
  }

  /**
   * Walks the ancestor chain, updating each parent's derived status; returns updated parent ids.
   *
   * @param testItem      the item whose change triggered the walk
   * @param launch        launch context for activity and final launch status
   * @param issueRequired whether parents may get a to-investigate issue
   * @param user          user for published activities
   * @param project       project and organization context
   * @return id of each parent that was materialized/updated, in walk order
   */
  protected List<Long> changeParentsStatuses(TestItem testItem, Launch launch,
      boolean issueRequired, ReportPortalUser user, Project project) {
    List<Long> updatedParents = Lists.newArrayList();

    Long parentId = testItem.getParentId();
    while (parentId != null) {

      TestItem parent = testItemRepository.findById(parentId).orElseThrow(
          () -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItem.getParentId()));

      StatusEnum currentParentStatus = parent.getItemResults().getStatus();
      if (!StatusEnum.IN_PROGRESS.equals(currentParentStatus)) {

        StatusEnum newParentStatus = evaluateParentItemStatus(parent, testItem);
        if (!currentParentStatus.equals(newParentStatus)) {
          TestItemActivityResource before =
              TO_ACTIVITY_RESOURCE.apply(parent, launch.getProjectId());
          parent.getItemResults().setStatus(newParentStatus);
          updateItem(parent, launch.getProjectId(), issueRequired).ifPresent(updatedParents::add);
          publishUpdateActivity(before, TO_ACTIVITY_RESOURCE.apply(parent, launch.getProjectId()),
              user, project.getOrganizationId()
          );
        } else {
          return updatedParents;
        }

      } else {
        return updatedParents;
      }

      parentId = parent.getParentId();
    }

    if (launch.getStatus() != IN_PROGRESS) {
      launch.setStatus(
          launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(), StatusEnum.PASSED.name(),
              INFO.name(), WARN.name()
          ) ? FAILED : PASSED);
    }

    return updatedParents;
  }

  private Optional<Long> updateItem(TestItem parent, Long projectId, boolean issueRequired) {
    if (parent.isHasStats() && !parent.isHasChildren()) {
      updateIssue(parent, projectId, issueRequired);
      return Optional.of(parent.getItemId());
    }
    return Optional.empty();
  }

  private void updateIssue(TestItem parent, Long projectId, boolean issueRequired) {
    if (issueRequired) {
      if (ofNullable(parent.getItemResults().getIssue()).isEmpty()) {
        addToInvestigateIssue(parent, projectId);
      }
    } else {
      ofNullable(parent.getItemResults().getIssue()).ifPresent(issue -> {
        issue.setTestItemResults(null);
        issueEntityRepository.delete(issue);
        parent.getItemResults().setIssue(null);
        logIndexer.indexItemsRemoveAsync(projectId, Collections.singletonList(parent.getItemId()));
      });
    }
  }

  private void publishUpdateActivity(TestItemActivityResource before,
      TestItemActivityResource after, ReportPortalUser user, Long orgId) {
    eventPublisher.publishEvent(
        new TestItemStatusChangedEvent(before, after, user.getUserId(), user.getUsername(), orgId));
  }

}
