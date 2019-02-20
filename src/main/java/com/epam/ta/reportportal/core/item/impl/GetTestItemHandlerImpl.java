/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * GET operations for {@link TestItem}<br>
 * Default implementation
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
@Service
class GetTestItemHandlerImpl implements GetTestItemHandler {

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final ItemAttributeRepository itemAttributeRepository;

	private final TestItemResourceAssembler itemResourceAssembler;

	@Autowired
	public GetTestItemHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, TestItemResourceAssembler itemResourceAssembler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.itemResourceAssembler = itemResourceAssembler;
	}

	@Override
	public TestItemResource getTestItem(Long testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		validate(testItem.getLaunch().getId(), projectDetails, user);
		return itemResourceAssembler.toResource(testItem);
	}

	@Override
	public Iterable<TestItemResource> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, Long launchId) {
		validate(launchId, projectDetails, user);
		Page<TestItem> testItemPage = testItemRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(itemResourceAssembler::toResource).apply(testItemPage);
	}

	@Override
	public List<String> getAttributeKeys(Long launchId, String value) {
		return itemAttributeRepository.findTestItemAttributeKeys(launchId, value, false);
	}

	@Override
	public List<String> getAttributeValues(Long launchId, String key, String value) {
		return itemAttributeRepository.findTestItemAttributeValues(launchId, key, value, false);
	}

	@Override
	public List<TestItemResource> getTestItems(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<TestItem> items;
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			items = testItemRepository.findByFilter(getItemsFilter(ids, projectDetails));
		} else {
			items = testItemRepository.findAllById(Arrays.asList(ids));
		}
		return items.stream().map(itemResourceAssembler::toResource).collect(Collectors.toList());
	}

	private void validate(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION, formattedSupplier(
					"Specified launch with id '{}' not referenced to specified project with id '{}'",
					launch.getId(),
					projectDetails.getProjectId()
			));
			expect(
					projectDetails.getProjectRole() == OPERATOR && launch.getMode() == LaunchModeEnum.DEBUG,
					Predicate.isEqual(false)
			).verify(ACCESS_DENIED);
		}
	}

	private Filter getItemsFilter(Long[] ids, ReportPortalUser.ProjectDetails projectDetails) {
		final Filter filter = Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build())
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_ID)
						.withCondition(Condition.IN)
						.withValue(Arrays.stream(ids).map(Object::toString).collect(Collectors.joining(",")))
						.build())
				.build();
		return projectDetails.getProjectRole() != ProjectRole.OPERATOR ?
				filter :
				filter.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, LaunchModeEnum.DEFAULT.name()).build());
	}
}
