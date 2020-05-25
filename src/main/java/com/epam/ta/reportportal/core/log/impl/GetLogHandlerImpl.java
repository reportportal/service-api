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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.NestedStep;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.LOG_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation of GET log operations
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLogHandlerImpl implements GetLogHandler {

	public static final String EXCLUDE_PASSED_LOGS = "excludePassedLogs";
	public static final String EXCLUDE_EMPTY_STEPS = "excludeEmptySteps";

	private final LogRepository logRepository;

	private final TestItemRepository testItemRepository;

	private final TestItemService testItemService;

	@Autowired
	public GetLogHandlerImpl(LogRepository logRepository, TestItemRepository testItemRepository, TestItemService testItemService) {
		this.logRepository = logRepository;
		this.testItemRepository = testItemRepository;
		this.testItemService = testItemService;
	}

	@Override
	public Iterable<LogResource> getLogs(ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		Page<Log> logPage = logRepository.findByFilter(filterable, pageable);
		return PagedResourcesAssembler.pageConverter(LogConverter.TO_RESOURCE).apply(logPage);
	}

	@Override
	public long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		return logRepository.getPageNumber(logId, filterable, pageable);
	}

	@Override
	public LogResource getLog(String logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Log log;
		try {
			log = findById(Long.parseLong(logId));
		} catch (NumberFormatException e) {
			log = findByUuid(logId);
		}
		validate(log, projectDetails);
		return LogConverter.TO_RESOURCE.apply(log);
	}

	@Override
	public Iterable<?> getNestedItems(Long parentId, ReportPortalUser.ProjectDetails projectDetails, Map<String, String> params,
			Queryable queryable, Pageable pageable) {

		TestItem parentItem = testItemRepository.findById(parentId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentId));
		Launch launch = testItemService.getEffectiveLaunch(parentItem);
		validate(launch, projectDetails);

		Boolean excludeEmptySteps = ofNullable(params.get(EXCLUDE_EMPTY_STEPS)).map(BooleanUtils::toBoolean).orElse(false);
		Boolean excludePassedLogs = ofNullable(params.get(EXCLUDE_PASSED_LOGS)).map(BooleanUtils::toBoolean).orElse(false);

		Page<NestedItem> nestedItems = logRepository.findNestedItems(parentId,
				excludeEmptySteps,
				isLogsExclusionRequired(parentItem, excludePassedLogs),
				queryable,
				pageable
		);

		List<NestedItem> content = nestedItems.getContent();

		Map<String, List<NestedItem>> result = content.stream().collect(groupingBy(NestedItem::getType));

		Map<Long, Log> logMap = ofNullable(result.get(LogRepositoryConstants.LOG)).map(logs -> logRepository.findAllById(logs.stream()
				.map(NestedItem::getId)
				.collect(Collectors.toSet())).stream().collect(toMap(Log::getId, l -> l))).orElseGet(Collections::emptyMap);

		queryable.getFilterConditions().add(getLaunchCondition(launch.getId()));
		queryable.getFilterConditions().add(getParentPathCondition(parentItem));
		Map<Long, NestedStep> nestedStepMap = ofNullable(result.get(LogRepositoryConstants.ITEM)).map(testItems -> testItemRepository.findAllNestedStepsByIds(
				testItems.stream().map(NestedItem::getId).collect(Collectors.toSet()),
				queryable,
				excludePassedLogs
		).stream().collect(toMap(NestedStep::getId, i -> i))).orElseGet(Collections::emptyMap);

		List<Object> resources = Lists.newArrayListWithExpectedSize(content.size());
		content.forEach(nestedItem -> {
			if (LogRepositoryConstants.LOG.equals(nestedItem.getType())) {
				ofNullable(logMap.get(nestedItem.getId())).map(LogConverter.TO_RESOURCE).ifPresent(resources::add);
			} else if (LogRepositoryConstants.ITEM.equals(nestedItem.getType())) {
				ofNullable(nestedStepMap.get(nestedItem.getId())).map(TestItemConverter.TO_NESTED_STEP_RESOURCE).ifPresent(resources::add);
			}
		});

		return PagedResourcesAssembler.pageConverter()
				.apply(PageableExecutionUtils.getPage(resources, nestedItems.getPageable(), nestedItems::getTotalElements));
	}

	/**
	 * Validate log item on existence, availability under specified project,
	 * etc.
	 *
	 * @param log            - log item
	 * @param projectDetails Project details
	 */
	private void validate(Log log, ReportPortalUser.ProjectDetails projectDetails) {
		Long launchProjectId = ofNullable(log.getTestItem()).map(it -> testItemService.getEffectiveLaunch(it).getProjectId())
				.orElseGet(() -> log.getLaunch().getProjectId());

		expect(launchProjectId, equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' is not under '{}' project", log.getId(), projectDetails.getProjectName())
		);
	}

	private void validate(Launch launch, ReportPortalUser.ProjectDetails projectDetails) {
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Launch '{}' is not under '{}' project", launch.getId(), projectDetails.getProjectName())
		);
	}

	/**
	 * Find log item by id
	 *
	 * @param logId - log ID
	 * @return - log item
	 */
	private Log findById(Long logId) {
		return logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));
	}

	/**
	 * Find log item by uuid
	 *
	 * @param logId - log UUID
	 * @return - log item
	 */
	private Log findByUuid(String logId) {
		return logRepository.findByUuid(logId).orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));
	}

	private FilterCondition getLaunchCondition(Long launchId) {
		return FilterCondition.builder().eq(CRITERIA_ITEM_LAUNCH_ID, String.valueOf(launchId)).build();
	}

	private FilterCondition getParentPathCondition(TestItem parent) {
		String pathValue = ofNullable(parent.getRetryOf()).flatMap(retryParentId -> ofNullable(parent.getParent()).map(retryParent ->
				retryParent.getPath() + "." + parent.getItemId())).orElse(parent.getPath());
		return FilterCondition.builder().withCondition(Condition.UNDER).withSearchCriteria(CRITERIA_PATH).withValue(pathValue).build();
	}

	/**
	 * Method to determine whether logs of the {@link TestItem} with {@link StatusEnum#PASSED}
	 * should be retrieved with nested steps or should be excluded from the select query
	 *
	 * @param parent            {@link Log#getTestItem()}
	 * @param excludePassedLogs if 'true' logs of the passed items should be excluded
	 * @return 'true' if logs should be excluded from the select query, else 'false'
	 */
	private boolean isLogsExclusionRequired(TestItem parent, boolean excludePassedLogs) {
		if (excludePassedLogs) {
			return Stream.of(StatusEnum.values()).filter(StatusEnum::isPositive).anyMatch(s -> s == parent.getItemResults().getStatus());
		}
		return false;
	}
}
