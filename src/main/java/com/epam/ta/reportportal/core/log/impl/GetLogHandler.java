/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.log.IGetLogHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
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
public class GetLogHandler implements IGetLogHandler {

	private final LogRepository logRepository;

	private final TestItemRepository testItemRepository;

	@Autowired
	public GetLogHandler(LogRepository logRepository, TestItemRepository testItemRepository) {
		this.logRepository = logRepository;
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Iterable<LogResource> getLogs(Long testStepId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable,
			Pageable pageable) {

		Page<Log> logs = logRepository.findByFilter(filterable, pageable);
		return PagedResourcesAssembler.pageConverter(LogConverter.TO_RESOURCE).apply(logs);
	}

	@Override
	public long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		return logRepository.getPageNumber(logId, filterable, pageable);
	}

	@Override
	public LogResource getLog(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Log log = findAndValidate(logId, projectDetails, user);

		return LogConverter.TO_RESOURCE.apply(log);
	}

	@Override
	public Iterable<?> getNestedItems(Long parentId, ReportPortalUser.ProjectDetails projectDetails, Queryable queryable,
			Pageable pageable) {
		Page<NestedItem> nestedItems = logRepository.findNestedItems(parentId, queryable, pageable);

		List<NestedItem> content = nestedItems.getContent();

		Map<String, List<NestedItem>> result = content.stream().collect(groupingBy(NestedItem::getType));

		Map<Long, Log> logMap = ofNullable(result.get(LogRepositoryConstants.LOG)).map(logs -> logRepository.findAllById(logs.stream()
				.map(NestedItem::getId)
				.collect(Collectors.toSet())).stream().collect(toMap(Log::getId, l -> l))).orElseGet(Collections::emptyMap);
		Map<Long, TestItem> testItemMap = ofNullable(result.get(LogRepositoryConstants.ITEM)).map(testItems -> testItemRepository.findAllById(
				testItems.stream().map(NestedItem::getId).collect(Collectors.toSet())).stream().collect(toMap(TestItem::getItemId, i -> i)))
				.orElseGet(Collections::emptyMap);

		List<Object> resources = Lists.newArrayListWithExpectedSize(content.size());
		content.forEach(nestedItem -> {
			if (LogRepositoryConstants.LOG.equals(nestedItem.getType())) {
				ofNullable(logMap.get(nestedItem.getId())).map(LogConverter.TO_RESOURCE).ifPresent(resources::add);
			} else if (LogRepositoryConstants.ITEM.equals(nestedItem.getType())) {
				ofNullable(testItemMap.get(nestedItem.getId())).map(TestItemConverter.TO_RESOURCE).ifPresent(resources::add);
			}
		});

		return PagedResourcesAssembler.pageConverter()
				.apply(PageableExecutionUtils.getPage(resources, nestedItems.getPageable(), nestedItems::getTotalElements));
	}

	/**
	 * Validate log item on existence, availability under specified project,
	 * etc.
	 *
	 * @param logId          - log ID
	 * @param projectDetails Project details
	 * @return Log - validate Log item in accordance with specified ID
	 */
	private Log findAndValidate(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Log log = logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));

		final TestItem testItem = log.getTestItem();
		Launch launch = testItem.getLaunch();

		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectDetails.getProjectId())
		);

		return log;
	}
}
