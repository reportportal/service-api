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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.analyzer.auto.SearchLogService;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.model.SearchRq;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.SearchMode;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SearchLogRq;
import com.epam.ta.reportportal.ws.model.log.SearchLogRs;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static java.util.stream.Collectors.toSet;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class SearchLogServiceImpl implements SearchLogService {

	private static final int LAUNCHES_FILTER_LIMIT = 10;

	private final ProjectRepository projectRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final UserFilterRepository userFilterRepository;

	@Autowired
	public SearchLogServiceImpl(ProjectRepository projectRepository, LaunchRepository launchRepository,
			TestItemRepository testItemRepository, LogRepository logRepository, AnalyzerServiceClient analyzerServiceClient,
			UserFilterRepository userFilterRepository) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.analyzerServiceClient = analyzerServiceClient;
		this.userFilterRepository = userFilterRepository;
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
		SearchMode searchMode = SearchMode.fromString(request.getSearchMode())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, request.getSearchMode()));

		SearchRq searchRq = searchMode == SearchMode.FILTER ? prepareFilter(request, project.getId()) : new SearchRq();
		List<String> logMessages = logRepository.findMessagesByItemIdAndLevelGte(item.getItemId(), LogLevel.ERROR_INT);
		if (CollectionUtils.isEmpty(logMessages)) {
			return Optional.empty();
		}
		searchRq.setLogMessages(logMessages);
		searchRq.setSearchConfig(SearchRq.SearchConfig.of(searchMode.getValue(),
				AnalyzerUtils.getAnalyzerConfig(project).getNumberOfLogLines()
		));
		searchRq.setItemId(item.getItemId());
		searchRq.setLaunchId(launch.getId());
		searchRq.setLaunchName(launch.getName());
		searchRq.setProjectId(project.getId());
		return Optional.of(searchRq);
	}

	private Collection<SearchLogRs> processRequest(SearchRq request) {
		List<Log> foundLogs = logRepository.findAllById(analyzerServiceClient.searchLogs(request));
		Map<Long, SearchLogRs> foundLogsMap = Maps.newHashMap();

		foundLogs.forEach(log -> {
			foundLogsMap.computeIfPresent(log.getTestItem().getItemId(), (key, value) -> {
				value.getLogMessages().add(log.getLogMessage());
				return value;
			});
			Launch launch = launchRepository.findById(log.getTestItem().getLaunchId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, log.getTestItem().getLaunchId()));
			Map<Long, String> pathNames = testItemRepository.selectPathNames(log.getTestItem().getPath());
			foundLogsMap.putIfAbsent(log.getTestItem().getItemId(), composeResponse(launch, log, pathNames));
		});
		return foundLogsMap.values();
	}

	private SearchRq prepareFilter(SearchLogRq request, Long projectId) {
		UserFilter userFilter = userFilterRepository.findByIdAndProjectId(request.getFilterId(), projectId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, request.getFilterId(), projectId));
		expect(userFilter.getTargetClass(), equalTo(ObjectType.Launch)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Filter type '{}' is not supported", userFilter.getTargetClass())
		);

		Filter filter = new Filter(userFilter.getTargetClass().getClassObject(), Lists.newArrayList(userFilter.getFilterCondition()));
		PageRequest pageable = PageRequest.of(0, LAUNCHES_FILTER_LIMIT, Sort.by(Sort.Direction.DESC, CRITERIA_START_TIME));
		List<Long> filteredLaunchIds = launchRepository.findByFilter(filter, pageable)
				.stream()
				.map(Launch::getId)
				.collect(Collectors.toList());

		SearchRq searchRq = new SearchRq();
		searchRq.setFilteredLaunchIds(filteredLaunchIds);
		return searchRq;
	}

	private SearchLogRs composeResponse(Launch launch, Log log, Map<Long, String> pathNames) {
		SearchLogRs response = new SearchLogRs();
		response.setLaunchId(launch.getId());
		response.setLaunchName(launch.getName() + " #" + launch.getNumber());
		response.setItemId(log.getTestItem().getItemId());
		response.setItemName(log.getTestItem().getName());
		response.setPath(log.getTestItem().getPath());
		response.setPathNames(pathNames);
		response.setPatternTemplates(log.getTestItem()
				.getPatternTemplateTestItems()
				.stream()
				.map(patternTemplateTestItem -> patternTemplateTestItem.getPatternTemplate().getName())
				.collect(toSet()));
		response.setDuration(log.getTestItem().getItemResults().getDuration());
		response.setStatus(log.getTestItem().getItemResults().getStatus().name());
		response.setIssue(IssueConverter.TO_MODEL.apply(log.getTestItem().getItemResults().getIssue()));
		response.setLogMessages(Arrays.asList(log.getLogMessage()));
		return response;
	}
}
