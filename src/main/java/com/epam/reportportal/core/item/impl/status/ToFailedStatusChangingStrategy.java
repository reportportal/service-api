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

package com.epam.reportportal.core.item.impl.status;

import static com.epam.reportportal.infrastructure.persistence.commons.Preconditions.statusIn;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;

import com.epam.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.reportportal.core.item.TestItemService;
import com.epam.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class ToFailedStatusChangingStrategy extends AbstractStatusChangingStrategy {

  @Autowired
  public ToFailedStatusChangingStrategy(TestItemService testItemService,
      ProjectRepository projectRepository, LaunchRepository launchRepository,
      TestItemRepository testItemRepository, IssueTypeHandler issueTypeHandler,
      ApplicationEventPublisher eventPublisher, IssueEntityRepository issueEntityRepository,
      LogRepository logRepository, LogIndexer logIndexer) {
    super(testItemService, projectRepository, launchRepository, testItemRepository,
        issueTypeHandler, eventPublisher, issueEntityRepository, logRepository, logIndexer
    );
  }

  @Override
  protected void updateStatus(Project project, Launch launch, TestItem testItem,
      StatusEnum providedStatus, ReportPortalUser user, boolean updateParents) {
    BusinessRule.expect(providedStatus, statusIn(StatusEnum.FAILED)).verify(INCORRECT_REQUEST,
        Suppliers.formattedSupplier("Incorrect status - '{}', only '{}' is allowed", providedStatus,
            StatusEnum.FAILED
        ).get()
    );

    testItem.getItemResults().setStatus(providedStatus);
    if (Objects.isNull(testItem.getRetryOf())) {
      if (testItem.getItemResults().getIssue() == null && testItem.isHasStats()) {
        addToInvestigateIssue(testItem, project.getId());
      }

      List<Long> itemsToReindex = new ArrayList<>();
      if (updateParents) {
        itemsToReindex = changeParentsStatuses(testItem, launch, true, user, project);
      }
      itemsToReindex.add(testItem.getItemId());
      logIndexer.indexItemsRemove(project.getId(), itemsToReindex);
      logIndexer.indexItemsLogs(project.getId(), launch.getId(), itemsToReindex,
          AnalyzerUtils.getAnalyzerConfig(project)
      );
    }
  }

  @Override
  protected StatusEnum evaluateParentItemStatus(TestItem parentItem, TestItem childItem) {
    return StatusEnum.FAILED;
  }

}
