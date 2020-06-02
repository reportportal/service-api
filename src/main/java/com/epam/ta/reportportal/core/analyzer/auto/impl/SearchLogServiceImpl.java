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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.SearchLogService;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.search.SearchCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.search.SearchLogsMode;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRq;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRs;
import com.epam.ta.reportportal.ws.model.log.SearchLogRq;
import com.epam.ta.reportportal.ws.model.log.SearchLogRs;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.TO_LOG_ENTRY;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class SearchLogServiceImpl implements SearchLogService {

	private final ProjectRepository projectRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final SearchCollectorFactory searchCollectorFactory;

	@Autowired
	public SearchLogServiceImpl(ProjectRepository projectRepository, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, LogRepository logRepository, AnalyzerServiceClient analyzerServiceClient,
			SearchCollectorFactory searchCollectorFactory) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.analyzerServiceClient = analyzerServiceClient;
		this.searchCollectorFactory = searchCollectorFactory;
	}

	@Override
	public Iterable<SearchLogRs> search(Long itemId, SearchLogRq request, ReportPortalUser.ProjectDetails projectDetails) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		TestItem item = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));

		Launch launch = launchRepository.findById(item.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, item.getLaunchId()));

		expect(item.getItemResults().getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(ErrorType.TEST_ITEM_IS_NOT_FINISHED);

		return composeRequest(request, project, item, launch).map(this::processRequest).orElse(Collections.emptyList());
	}

	private Optional<SearchRq> composeRequest(SearchLogRq request, Project project, TestItem item, Launch launch) {
		SearchLogsMode searchMode = SearchLogsMode.fromString(request.getSearchMode())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, request.getSearchMode()));

		SearchRq searchRq = new SearchRq();

		searchRq.setFilteredLaunchIds(searchCollectorFactory.getCollector(searchMode).collect(request.getFilterId(), launch));

		//TODO fix query - select messages from `Nested Step` descendants too
		List<String> logMessages = logRepository.findMessagesByItemIdAndLevelGte(item.getItemId(), LogLevel.ERROR_INT);
		if (CollectionUtils.isEmpty(logMessages)) {
			return Optional.empty();
		}
		searchRq.setLogMessages(logMessages);
		searchRq.setLogLines(AnalyzerUtils.getAnalyzerConfig(project).getNumberOfLogLines());
		searchRq.setItemId(item.getItemId());
		searchRq.setLaunchId(launch.getId());
		searchRq.setLaunchName(launch.getName());
		searchRq.setProjectId(project.getId());
		return Optional.of(searchRq);
	}

	private Collection<SearchLogRs> processRequest(SearchRq request) {
		List<SearchRs> searchRs = analyzerServiceClient.searchLogs(request);
		Map<Long, Long> logIdMapping = searchRs.stream()
				.collect(HashMap::new, (m, rs) -> m.put(rs.getLogId(), rs.getTestItemId()), Map::putAll);
		Map<Long, TestItem> testItemMapping = testItemRepository.findAllById(logIdMapping.values())
				.stream()
				.collect(toMap(TestItem::getItemId, item -> item));
		List<Log> foundLogs = logRepository.findAllById(logIdMapping.keySet());
		Map<Long, SearchLogRs> foundLogsMap = Maps.newHashMap();

		foundLogs.forEach(log -> ofNullable(logIdMapping.get(log.getId())).ifPresent(itemId -> {
			foundLogsMap.computeIfPresent(itemId, (key, value) -> {
				value.getLogs().add(TO_LOG_ENTRY.apply(log));
				return value;
			});
			foundLogsMap.computeIfAbsent(itemId, key -> composeResponse(testItemMapping, itemId, log));
		}));
		return foundLogsMap.values();
	}

	private SearchLogRs composeResponse(Map<Long, TestItem> testItemMapping, Long itemId, Log log) {
		TestItem testItem = ofNullable(testItemMapping.get(itemId)).orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND,
				itemId
		));
		Long launchId = ofNullable(testItem.getLaunchId()).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
				testItem.getLaunchId()
		));
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		Map<Long, String> pathNames = testItemRepository.selectPathNames(testItem.getPath());

		SearchLogRs response = new SearchLogRs();
		response.setLaunchId(launch.getId());
		response.setLaunchName(launch.getName() + " #" + launch.getNumber());
		response.setItemId(testItem.getItemId());
		response.setItemName(testItem.getName());
		response.setPath(testItem.getPath());
		response.setPathNames(pathNames);
		response.setPatternTemplates(testItem.getPatternTemplateTestItems()
				.stream()
				.map(patternTemplateTestItem -> patternTemplateTestItem.getPatternTemplate().getName())
				.collect(toSet()));
		response.setDuration(testItem.getItemResults().getDuration());
		response.setStatus(testItem.getItemResults().getStatus().name());

		TestItem itemWithStats = testItem;
		while (!itemWithStats.isHasStats()) {
			itemWithStats = itemWithStats.getParent();
		}

		response.setIssue(IssueConverter.TO_MODEL.apply(itemWithStats.getItemResults().getIssue()));
		response.setLogs(Lists.newArrayList(TO_LOG_ENTRY.apply(log)));
		return response;
	}
}
