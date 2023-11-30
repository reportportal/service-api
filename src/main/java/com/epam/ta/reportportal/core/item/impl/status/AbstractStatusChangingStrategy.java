/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl.status;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AbstractStatusChangingStrategy implements StatusChangingStrategy {

  private final TestItemService testItemService;

  private final ProjectRepository projectRepository;
  private final LaunchRepository launchRepository;
  private final IssueTypeHandler issueTypeHandler;
  private final MessageBus messageBus;

  protected final TestItemRepository testItemRepository;
  protected final IssueEntityRepository issueEntityRepository;
  protected final LogRepository logRepository;
  protected final LogIndexer logIndexer;

  protected AbstractStatusChangingStrategy(TestItemService testItemService,
      ProjectRepository projectRepository, LaunchRepository launchRepository,
      TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
      MessageBus messageBus, IssueEntityRepository issueEntityRepository,
      LogRepository logRepository, LogIndexer logIndexer) {
    this.testItemService = testItemService;
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.issueTypeHandler = issueTypeHandler;
    this.messageBus = messageBus;
    this.issueEntityRepository = issueEntityRepository;
    this.logRepository = logRepository;
    this.logIndexer = logIndexer;
  }

  protected abstract void updateStatus(Project project, Launch launch, TestItem testItem,
      StatusEnum providedStatus, ReportPortalUser user);

  protected abstract StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem);

  @Override
  public void changeStatus(TestItem testItem, StatusEnum providedStatus, ReportPortalUser user) {
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
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, launch.getProjectId()));

    updateStatus(project, launch, testItem, providedStatus, user);
  }

  protected void addToInvestigateIssue(TestItem testItem, Long projectId) {
    IssueEntity issueEntity = new IssueEntity();
    IssueType toInvestigate =
        issueTypeHandler.defineIssueType(projectId, TO_INVESTIGATE.getLocator());
    issueEntity.setIssueType(toInvestigate);
    issueEntity.setTestItemResults(testItem.getItemResults());
    issueEntityRepository.save(issueEntity);
    testItem.getItemResults().setIssue(issueEntity);
  }

}
