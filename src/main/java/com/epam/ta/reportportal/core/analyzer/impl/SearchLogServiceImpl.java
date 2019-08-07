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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.analyzer.SearchLogService;
import com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.model.SearchRq;
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
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SearchLogRq;
import com.epam.ta.reportportal.ws.model.log.SearchLogRs;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.TO_SEARCH_LOG_RS;

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

		expect(item.getItemResults().getStatus(), not(statusIn(StatusEnum.IN_PROGRESS))).verify(ErrorType.UNSUPPORTED_TEST_ITEM_TYPE,
				item.getItemResults().getStatus()
		);

		SearchMode searchMode = SearchMode.fromString(request.getSearchMode())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, request.getSearchMode()));

		SearchRq searchRq = searchMode == SearchMode.FILTER ? prepareFilter(request, project.getId()) : new SearchRq();
		searchRq.setSearchConfig(SearchRq.SearchConfig.of(searchMode.getValue(),
				AnalyzerUtils.getAnalyzerConfig(project).getNumberOfLogLines()
		));
		searchRq.setItemId(item.getItemId());
		searchRq.setLaunchId(item.getLaunch().getId());
		searchRq.setLaunchName(item.getLaunch().getName());
		searchRq.setProjectId(project.getId());

		List<String> logMessages = logRepository.findMessagesByItemIdAndLevelGte(item.getItemId(), LogLevel.ERROR_INT);
		if (CollectionUtils.isEmpty(logMessages)) {
			return Collections.emptyList();
		}
		searchRq.setLogMessages(logMessages);

		List<Long> foundLogIds = analyzerServiceClient.searchLogs(searchRq);
		List<Log> foundLogs = logRepository.findAllById(foundLogIds);

		Map<Long, SearchLogRs> foundLogsMap = Maps.newHashMap();
		foundLogs.forEach(it -> {
			foundLogsMap.computeIfPresent(it.getTestItem().getItemId(), (k, v) -> {
				v.getLogMessages().add(it.getLogMessage());
				return v;
			});
			foundLogsMap.putIfAbsent(it.getTestItem().getItemId(), TO_SEARCH_LOG_RS.apply(it));
		});

		return foundLogsMap.values();
	}

	private SearchRq prepareFilter(SearchLogRq request, Long projectId) {
		UserFilter filter = userFilterRepository.findByIdAndProjectId(request.getFilterId(), projectId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, request.getFilterId(), projectId));
		expect(filter.getTargetClass(), equalTo(ObjectType.Launch)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Filter type '{}' is not supported", filter.getTargetClass())
		);

		List<Long> filteredLaunchIds = launchRepository.findByFilter(new Filter(filter.getTargetClass().getClassObject(),
				filter.getFilterCondition()
		))
				.stream()
				.map(Launch::getId)
				.collect(Collectors.toList());

		SearchRq searchRq = new SearchRq();
		searchRq.setFilteredLaunchIds(filteredLaunchIds);
		return searchRq;
	}
}
