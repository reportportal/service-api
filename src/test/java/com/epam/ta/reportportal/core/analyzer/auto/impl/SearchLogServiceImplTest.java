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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import static com.epam.ta.reportportal.core.analyzer.auto.strategy.search.SearchLogsMode.CURRENT_LAUNCH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.search.CurrentLaunchCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.search.SearchCollectorFactory;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.model.analyzer.SearchRq;
import com.epam.ta.reportportal.model.analyzer.SearchRs;
import com.epam.ta.reportportal.model.log.SearchLogRq;
import com.epam.ta.reportportal.model.log.SearchLogRs;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class SearchLogServiceImplTest {

  private final Project project = mock(Project.class);
  private final Launch launch = mock(Launch.class);
  private final TestItem testItem = mock(TestItem.class);
  private final TestItem testItemOfFoundLog = mock(TestItem.class);
  private final TestItemResults testItemResults = mock(TestItemResults.class);
  private final UserFilter userFilter = mock(UserFilter.class);

  private final ProjectRepository projectRepository = mock(ProjectRepository.class);

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final LogService logService = mock(LogService.class);

  private final AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

  private final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);

  private SearchCollectorFactory searchCollectorFactory = mock(SearchCollectorFactory.class);

  private CurrentLaunchCollector currentLaunchCollector = mock(CurrentLaunchCollector.class);

  private final SearchLogServiceImpl searchLogService =
      new SearchLogServiceImpl(projectRepository, launchRepository, testItemRepository, logService,
          analyzerServiceClient, searchCollectorFactory
      );

  @Test
  void searchTest() {

    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withOrgId(1L)
        .withOrgName("org Name")
        .withOrgRole(OrganizationRole.MANAGER)
        .withProjectId(1L)
        .withProjectRole(ProjectRole.EDITOR)
        .withProjectKey("project-key")
        .withProjectName("name")
        .build();

    when(projectRepository.findById(membershipDetails.getProjectId())).thenReturn(
        Optional.of(project));
    when(testItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(testItemRepository.findAllById(any())).thenReturn(Lists.newArrayList(testItemOfFoundLog));
    when(testItem.getLaunchId()).thenReturn(1L);
    when(testItemOfFoundLog.getItemId()).thenReturn(2L);
    when(testItemOfFoundLog.getLaunchId()).thenReturn(1L);
    when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));
    when(launch.getId()).thenReturn(1L);
    when(testItem.getPath()).thenReturn("1");
    when(testItem.getItemResults()).thenReturn(testItemResults);
    when(testItem.isHasStats()).thenReturn(true);
    when(testItemOfFoundLog.getItemResults()).thenReturn(testItemResults);
    when(testItemOfFoundLog.isHasStats()).thenReturn(true);

    when(testItemResults.getStatus()).thenReturn(StatusEnum.FAILED);

    IssueType issueType = new IssueType();
    issueType.setLocator("locator");
    IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIssueType(issueType);
    issueEntity.setIgnoreAnalyzer(false);
    when(testItemResults.getIssue()).thenReturn(issueEntity);

    when(userFilterRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.of(userFilter));
    when(userFilter.getTargetClass()).thenReturn(ObjectType.Launch);
    when(userFilter.getFilterCondition()).thenReturn(Collections.emptySet());

    when(logService.findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(launch.getId(),
        testItem.getItemId(), testItem.getPath(), LogLevel.ERROR_INT
    )).thenReturn(Lists.newArrayList("message"));
    SearchRs searchRs = new SearchRs();
    searchRs.setLogId(1L);
    searchRs.setTestItemId(2L);
    when(analyzerServiceClient.searchLogs(any(SearchRq.class))).thenReturn(
        Lists.newArrayList(searchRs));
    LogFull log = new LogFull();
    log.setId(1L);
    log.setTestItem(testItem);
    log.setLogMessage("message");
    log.setLogLevel(40000);
    when(logService.findAllById(any())).thenReturn(Lists.newArrayList(log));

    SearchLogRq searchLogRq = new SearchLogRq();
    searchLogRq.setSearchMode(CURRENT_LAUNCH.getValue());
    searchLogRq.setFilterId(1L);

    when(searchCollectorFactory.getCollector(CURRENT_LAUNCH)).thenReturn(currentLaunchCollector);
    when(currentLaunchCollector.collect(any(), any())).thenReturn(Collections.singletonList(1L));

    Iterable<SearchLogRs> responses = searchLogService.search(1L, searchLogRq, membershipDetails);
    Assertions.assertNotNull(responses);
    Assertions.assertEquals(1, Lists.newArrayList(responses).size());

  }
}
