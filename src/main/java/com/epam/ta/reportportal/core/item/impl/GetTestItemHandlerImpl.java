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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.LAUNCHES_COUNT;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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

	private final TicketRepository ticketRepository;

	private final GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@Autowired
	public GetTestItemHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			ItemAttributeRepository itemAttributeRepository, TestItemResourceAssembler itemResourceAssembler,
			TicketRepository ticketRepository, GetShareableEntityHandler<UserFilter> getShareableEntityHandler) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.itemResourceAssembler = itemResourceAssembler;
		this.ticketRepository = ticketRepository;
		this.getShareableEntityHandler = getShareableEntityHandler;
	}

	@Override
	public TestItemResource getTestItem(Long testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findById(testItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		validate(testItem.getLaunchId(), projectDetails, user);
		return itemResourceAssembler.toResource(testItem);
	}

	@Override
	public TestItemResource getTestItem(String testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem = testItemRepository.findByUuid(testItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		validate(testItem.getLaunchId(), projectDetails, user);
		return itemResourceAssembler.toResource(testItem);
	}

	@Override
	public Iterable<TestItemResource> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, @Nullable Long launchId, @Nullable Long filterId, int launchesLimit) {

		Optional<Long> launchIdOptional = Optional.ofNullable(launchId);
		Optional<Long> filterIdOptional = Optional.ofNullable(filterId);

		Page<TestItem> testItemPage = filterIdOptional.map(launchFilterId -> {
			validateProjectRole(projectDetails, user);
			return getItemsWithLaunchesFiltering(filter, pageable, projectDetails, launchFilterId, launchesLimit);
		}).orElseGet(() -> launchIdOptional.map(id -> {
			validate(id, projectDetails, user);
			return testItemRepository.findByFilter(filter, pageable);
		}).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Neither launch nor filter id specified.")));

		Map<Long, Map<Long, String>> pathNamesMapping = getPathNamesMapping(testItemPage.getContent());

		return PagedResourcesAssembler.<TestItem, TestItemResource>pageConverter(item -> itemResourceAssembler.toResource(item,
				pathNamesMapping.get(item.getItemId())
		)).apply(testItemPage);
	}

	@Override
	public List<String> getTicketIds(Long launchId, String term) {
		BusinessRule.expect(term.length() > 2, Predicates.equalTo(true))
				.verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
						Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", term)
				);
		return ticketRepository.findByTerm(launchId, term);
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
		return items.stream().map(itemResourceAssembler::toResource).collect(toList());
	}

	private void validate(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					formattedSupplier("Specified launch with id '{}' not referenced to specified project with id '{}'",
							launch.getId(),
							projectDetails.getProjectId()
					)
			);
			expect(projectDetails.getProjectRole() == OPERATOR && launch.getMode() == LaunchModeEnum.DEBUG,
					Predicate.isEqual(false)
			).verify(ACCESS_DENIED);
		}
	}

	private void validateProjectRole(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectRole() == OPERATOR, Predicate.isEqual(false)).verify(ACCESS_DENIED);
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

	private Page<TestItem> getItemsWithLaunchesFiltering(Queryable testItemFilter, Pageable testItemPageable,
			ReportPortalUser.ProjectDetails projectDetails, Long launchFilterId, int launchesLimit) {
		UserFilter userFilter = getShareableEntityHandler.getPermitted(launchFilterId, projectDetails);
		Queryable launchFilter = createLaunchFilter(projectDetails, userFilter);
		Pageable launchPageable = createLaunchPageable(userFilter, launchesLimit);
		return testItemRepository.findByFilter(launchFilter, testItemFilter, launchPageable, testItemPageable);
	}

	private Filter createLaunchFilter(ReportPortalUser.ProjectDetails projectDetails, UserFilter launchFilter) {

		BusinessRule.expect(launchFilter, f -> ObjectType.Launch.equals(f.getTargetClass()))
				.verify(ErrorType.BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("Incorrect filter target - '{}'. Allowed: '{}'",
								launchFilter.getTargetClass(),
								ObjectType.Launch
						)
				);

		Filter filter = Filter.builder()
				.withTarget(launchFilter.getTargetClass().getClassObject())
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build())
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.NOT_EQUALS)
						.withSearchCriteria(CRITERIA_LAUNCH_STATUS)
						.withValue(StatusEnum.IN_PROGRESS.name())
						.build())
				.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, Mode.DEFAULT.toString()).build())
				.build();
		filter.getFilterConditions().addAll(launchFilter.getFilterCondition());
		return filter;
	}

	private Pageable createLaunchPageable(UserFilter launchFilter, int launchesLimit) {

		BusinessRule.expect(launchesLimit, limit -> limit > 0 && limit <= LAUNCHES_COUNT)
				.verify(ErrorType.BAD_REQUEST_ERROR, "Launches limit should be greater than 0 and less or equal to 600");

		Sort sort = ofNullable(launchFilter.getFilterSorts()).map(sorts -> Sort.by(sorts.stream()
				.map(s -> Sort.Order.by(s.getField()).with(s.getDirection()))
				.collect(toList()))).orElseGet(Sort::unsorted);
		return PageRequest.of(0, launchesLimit, sort);
	}

	private Map<Long, Map<Long, String>> getPathNamesMapping(List<TestItem> testItems) {
		return testItemRepository.selectPathNames(testItems.stream().map(TestItem::getItemId).collect(toList()));
	}
}
