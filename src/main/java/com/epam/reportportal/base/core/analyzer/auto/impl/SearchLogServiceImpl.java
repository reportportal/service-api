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

package com.epam.reportportal.base.core.analyzer.auto.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Preconditions.statusIn;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.not;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.base.core.analyzer.auto.SearchLogService;
import com.epam.reportportal.base.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.reportportal.base.core.analyzer.auto.strategy.search.SearchCollectorFactory;
import com.epam.reportportal.base.core.analyzer.auto.strategy.search.SearchLogsMode;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.PathName;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.analyzer.SearchRq;
import com.epam.reportportal.base.model.analyzer.SearchRs;
import com.epam.reportportal.base.model.log.SearchLogRq;
import com.epam.reportportal.base.model.log.SearchLogRs;
import com.epam.reportportal.base.ws.converter.converters.IssueConverter;
import com.epam.reportportal.base.ws.converter.converters.LogConverter;
import com.epam.reportportal.base.ws.converter.converters.TestItemConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads logs for search using analyzer and DB collectors.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

  private final ProjectRepository projectRepository;

  private final LaunchRepository launchRepository;

  private final TestItemRepository testItemRepository;

  private final LogService logService;

  private final AnalyzerServiceClient analyzerServiceClient;

  private final SearchCollectorFactory searchCollectorFactory;

  private final LogConverter logConverter;

  @Override
  public Iterable<SearchLogRs> search(Long itemId, SearchLogRq request,
      MembershipDetails membershipDetails) {
    Project project = projectRepository.findById(membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Project " + membershipDetails.getProjectId()));

    TestItem item = testItemRepository.findById(itemId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));

    Launch launch = launchRepository.findById(item.getLaunchId())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, item.getLaunchId()));

    expect(item.getItemResults().getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(
        ErrorType.TEST_ITEM_IS_NOT_FINISHED);

    return composeRequest(request, project, item, launch).map(
            searchRq -> processRequest(project.getId(), searchRq))
        .orElse(Collections.emptyList());
  }

  private Optional<SearchRq> composeRequest(SearchLogRq request, Project project, TestItem item,
      Launch launch) {
    SearchLogsMode searchMode = SearchLogsMode.fromString(request.getSearchMode())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, request.getSearchMode()));

    SearchRq searchRq = new SearchRq();

    searchRq.setFilteredLaunchIds(
        searchCollectorFactory.getCollector(searchMode).collect(request.getFilterId(), launch));

    //TODO fix query - select messages from `Nested Step` descendants too
    List<String> logMessages = logService.findMessagesByLaunchIdAndItemIdAndPathAndLevelGte(
        launch.getId(),
        item.getItemId(),
        item.getPath(),
        LogLevel.ERROR_INT
    );
    if (CollectionUtils.isEmpty(logMessages)) {
      return Optional.empty();
    }
    searchRq.setLogMessages(logMessages);

    final AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);
    searchRq.setAnalyzerConfig(analyzerConfig);
    searchRq.setLogLines(analyzerConfig.getNumberOfLogLines());
    searchRq.setItemId(item.getItemId());
    searchRq.setLaunchId(launch.getId());
    searchRq.setLaunchName(launch.getName());
    searchRq.setProjectId(project.getId());
    return Optional.of(searchRq);
  }

  private Collection<SearchLogRs> processRequest(Long projectId, SearchRq request) {
    List<SearchRs> searchRs = analyzerServiceClient.searchLogs(request);
    Map<Long, Long> logIdMapping = searchRs.stream()
        .collect(HashMap::new, (m, rs) -> m.put(rs.getLogId(), rs.getTestItemId()), Map::putAll);
    Map<Long, TestItem> testItemMapping = testItemRepository.findAllById(logIdMapping.values())
        .stream()
        .collect(toMap(TestItem::getItemId, item -> item));
    List<LogFull> foundLogs = logService.findAllById(logIdMapping.keySet());
    List<SearchLogRs.LogEntry> logEntries = logConverter.toLogEntries(foundLogs, projectId);

    Map<Long, SearchLogRs.LogEntry> logEntryByLogId = IntStream.range(0, foundLogs.size())
        .boxed()
        .collect(toMap(i -> foundLogs.get(i).getId(), logEntries::get));

    Map<Long, SearchLogRs> foundLogsMap = Maps.newHashMap();

    foundLogs.forEach(log -> ofNullable(logIdMapping.get(log.getId())).ifPresent(itemId -> {
      SearchLogRs.LogEntry logEntry = logEntryByLogId.get(log.getId());
      foundLogsMap.computeIfPresent(itemId, (key, value) -> {
        value.getLogs().add(logEntry);
        return value;
      });
      foundLogsMap.computeIfAbsent(itemId,
          key -> composeResponse(testItemMapping, projectId, itemId, logEntry));
    }));
    return foundLogsMap.values();
  }

  private SearchLogRs composeResponse(Map<Long, TestItem> testItemMapping, Long projectId,
      Long itemId, SearchLogRs.LogEntry logEntry) {
    TestItem testItem = ofNullable(testItemMapping.get(itemId)).orElseThrow(
        () -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND,
            itemId
        ));
    Long launchId = ofNullable(testItem.getLaunchId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
            testItem.getLaunchId()
        ));
    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

    Map<Long, PathName> pathNameMapping = testItemRepository.selectPathNames(
        singletonList(testItem));

    SearchLogRs response = new SearchLogRs();
    response.setLaunchId(launch.getId());
    ofNullable(pathNameMapping.get(testItem.getItemId())).ifPresent(pathName -> {
      response.setPathNames(TestItemConverter.PATH_NAME_TO_RESOURCE.apply(pathName));
    });
    response.setItemId(testItem.getItemId());
    response.setItemName(testItem.getName());
    response.setPath(testItem.getPath());
    response.setPatternTemplates(testItem.getPatternTemplateTestItems()
        .stream()
        .map(patternTemplateTestItem -> patternTemplateTestItem.getPatternTemplate().getName())
        .collect(toSet()));
    response.setDuration(
        ofNullable(testItem.getItemResults().getDuration()).orElseGet(() -> getDuration(testItem)));
    response.setStatus(testItem.getItemResults().getStatus().name());
    TestItem itemWithStats = testItem;
    while (!itemWithStats.isHasStats()) {
      final Long parentId = itemWithStats.getParentId();
      itemWithStats = testItemRepository.findById(parentId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentId));
    }

    response.setIssue(IssueConverter.TO_MODEL.apply(itemWithStats.getItemResults().getIssue()));
    response.setLogs(Lists.newArrayList(logEntry));
    return response;
  }

  private double getDuration(TestItem testItem) {
    return
        ChronoUnit.MILLIS.between(testItem.getStartTime(), testItem.getItemResults().getEndTime())
            / 1000d;
  }
}
