/*
 * Copyright 2021 EPAM Systems
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

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.epam.ta.reportportal.entity.enums.LogLevel.ERROR_INT;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestRq;
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.item.validator.state.TestItemValidator;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.GetClusterHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class SuggestItemService {

  private static final int SUGGESTED_ITEMS_LOGS_LIMIT = 5;

  private final AnalyzerServiceClient analyzerServiceClient;

  private final GetProjectHandler getProjectHandler;
  private final GetLaunchHandler getLaunchHandler;
  private final GetClusterHandler getClusterHandler;

  private final LaunchAccessValidator launchAccessValidator;

  private final TestItemRepository testItemRepository;
  private final LogService logService;

  private final List<TestItemValidator> testItemValidators;

  @Autowired
  public SuggestItemService(AnalyzerServiceClient analyzerServiceClient,
      GetProjectHandler getProjectHandler,
      GetLaunchHandler getLaunchHandler, GetClusterHandler getClusterHandler,
      LaunchAccessValidator launchAccessValidator,
      TestItemRepository testItemRepository, LogService logService,
      List<TestItemValidator> testItemValidators) {
    this.analyzerServiceClient = analyzerServiceClient;
    this.getProjectHandler = getProjectHandler;
    this.getLaunchHandler = getLaunchHandler;
    this.getClusterHandler = getClusterHandler;
    this.launchAccessValidator = launchAccessValidator;
    this.testItemRepository = testItemRepository;
    this.logService = logService;
    this.testItemValidators = testItemValidators;
  }

  @Transactional(readOnly = true)
  public List<SuggestedItem> suggestItems(Long testItemId,
      ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

    TestItem testItem = testItemRepository.findById(testItemId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));

    validateTestItem(testItem);

    Launch launch = getLaunch(testItem.getLaunchId(), projectDetails, user);
    Project project = getProjectHandler.get(projectDetails);

    SuggestRq suggestRq = prepareSuggestRq(testItem, launch, project);
    return getSuggestedItems(suggestRq);
  }

  private void validateTestItem(TestItem testItem) {
    testItemValidators.forEach(v -> {
      if (!v.validate(testItem)) {
        throw new ReportPortalException(BAD_REQUEST_ERROR, v.provide(testItem));
      }
    });
  }

  @Transactional(readOnly = true)
  public List<SuggestedItem> suggestClusterItems(Long clusterId,
      ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
    final Cluster cluster = getClusterHandler.getById(clusterId);
    final Launch launch = getLaunch(cluster.getLaunchId(), projectDetails, user);
    final Project project = getProjectHandler.get(projectDetails);
    final SuggestRq suggestRq = prepareSuggestRq(cluster, launch, project);
    return getSuggestedItems(suggestRq);
  }

  private Launch getLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {
    Launch launch = getLaunchHandler.get(launchId);
    launchAccessValidator.validate(launch, projectDetails, user);
    return launch;
  }

  private SuggestRq prepareSuggestRq(TestItem testItem, Launch launch, Project project) {
    SuggestRq suggestRq = prepareSuggestRq(launch, project);
    suggestRq.setTestItemId(testItem.getItemId());
    suggestRq.setUniqueId(testItem.getUniqueId());
    suggestRq.setTestCaseHash(testItem.getTestCaseHash());
    suggestRq.setLogs(AnalyzerUtils.fromLogs(
        logService.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launch.getId(),
            Collections.singletonList(testItem.getItemId()),
            ERROR_INT
        )));
    return suggestRq;
  }

  private SuggestRq prepareSuggestRq(Cluster cluster, Launch launch, Project project) {
    SuggestRq suggestRq = prepareSuggestRq(launch, project);
    suggestRq.setClusterId(cluster.getIndexId());
    return suggestRq;
  }

  private SuggestRq prepareSuggestRq(Launch launch, Project project) {
    SuggestRq suggestRq = new SuggestRq();
    suggestRq.setLaunchId(launch.getId());
    suggestRq.setLaunchName(launch.getName());
    suggestRq.setProject(project.getId());
    suggestRq.setAnalyzerConfig(getAnalyzerConfig(project));
    suggestRq.setLaunchNumber(launch.getNumber());
    return suggestRq;
  }

  private List<SuggestedItem> getSuggestedItems(SuggestRq suggestRq) {
    return analyzerServiceClient.searchSuggests(suggestRq)
        .stream()
        .map(this::prepareSuggestedItem)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private SuggestedItem prepareSuggestedItem(SuggestInfo suggestInfo) {
    TestItem relevantTestItem = testItemRepository.findById(suggestInfo.getRelevantItem())
        .orElse(null);
    //TODO: EPMRPP-61038 temp fix for the case when item was removed from db but still exists in elastic
    if (relevantTestItem == null) {
      return null;
    }
    SuggestedItem suggestedItem = new SuggestedItem();
    roundSuggestInfoMatchScore(suggestInfo);
    suggestedItem.setSuggestRs(suggestInfo);
    suggestedItem.setTestItemResource(TestItemConverter.TO_RESOURCE.apply(relevantTestItem));
    suggestedItem.setLogs(logService.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
        relevantTestItem.getLaunchId(),
        relevantTestItem.getItemId(),
        ERROR_INT,
        SUGGESTED_ITEMS_LOGS_LIMIT
    ).stream().map(LogConverter.TO_RESOURCE).collect(Collectors.toSet()));
    return suggestedItem;
  }

  private void roundSuggestInfoMatchScore(SuggestInfo info) {
    float roundedMatchScore = Math.round(info.getMatchScore());
    info.setMatchScore(roundedMatchScore);
  }

  public OperationCompletionRS handleSuggestChoice(List<SuggestInfo> suggestInfos) {
    analyzerServiceClient.handleSuggestChoice(suggestInfos);
    return new OperationCompletionRS("User choice of suggested item was sent for handling to ML");
  }
}
