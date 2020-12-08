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
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.utils.DefaultLaunchFilterProvider;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.StatisticsConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdaterProvider;
import com.epam.ta.reportportal.ws.converter.utils.item.content.TestItemUpdaterContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.statistics.StatisticsResource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
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

	private final TestItemRepository testItemRepository;

	private final TestItemService testItemService;

	private final LaunchAccessValidator launchAccessValidator;

	private final ItemAttributeRepository itemAttributeRepository;

	private final List<ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource>> resourceUpdaterProviders;

	private final TicketRepository ticketRepository;

	private final GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@Autowired
	public GetTestItemHandlerImpl(TestItemRepository testItemRepository, TestItemService testItemService, LaunchAccessValidator launchAccessValidator,
			ItemAttributeRepository itemAttributeRepository,
			List<ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource>> resourceUpdaterProviders,
			TicketRepository ticketRepository, GetShareableEntityHandler<UserFilter> getShareableEntityHandler1) {
		this.testItemRepository = testItemRepository;
		this.testItemService = testItemService;
		this.launchAccessValidator = launchAccessValidator;
		this.itemAttributeRepository = itemAttributeRepository;
		this.resourceUpdaterProviders = resourceUpdaterProviders;
		this.ticketRepository = ticketRepository;
		this.getShareableEntityHandler = getShareableEntityHandler1;
	}

	@Override
	public TestItemResource getTestItem(String testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem testItem;
		try {
			testItem = testItemRepository.findById(Long.parseLong(testItemId))
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		} catch (NumberFormatException e) {
			testItem = testItemRepository.findByUuid(testItemId)
					.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, testItemId));
		}

		Launch launch = testItemService.getEffectiveLaunch(testItem);
		launchAccessValidator.validate(launch.getId(), projectDetails, user);

		List<ResourceUpdater<TestItemResource>> resourceUpdaters = getResourceUpdaters(projectDetails.getProjectId(),
				Collections.singletonList(testItem)
		);
		TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(testItem);
		resourceUpdaters.forEach(updater -> updater.updateResource(testItemResource));
		return testItemResource;
	}

	@Override
	public Iterable<TestItemResource> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, @Nullable Long launchId, @Nullable Long filterId, boolean isLatest, int launchesLimit) {

		Optional<Long> launchIdOptional = Optional.ofNullable(launchId);
		Optional<Long> filterIdOptional = Optional.ofNullable(filterId);

		Page<TestItem> testItemPage = filterIdOptional.map(launchFilterId -> {
			validateProjectRole(projectDetails, user);
			return getItemsWithLaunchesFiltering(filter, pageable, projectDetails, launchFilterId, isLatest, launchesLimit);
		}).orElseGet(() -> launchIdOptional.map(id -> {
			launchAccessValidator.validate(id, projectDetails, user);
			return testItemRepository.findByFilter(filter, pageable);
		}).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Neither launch nor filter id specified.")));

		return PagedResourcesAssembler.<TestItem, TestItemResource>pageMultiConverter(items -> {
			List<ResourceUpdater<TestItemResource>> resourceUpdaters = getResourceUpdaters(projectDetails.getProjectId(),
					testItemPage.getContent()
			);
			return items.stream().map(item -> {
				TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(item);
				resourceUpdaters.forEach(updater -> updater.updateResource(testItemResource));
				return testItemResource;
			}).collect(toList());
		}).apply(testItemPage);
	}

	@Override
	public StatisticsResource getStatisticsByFilter(Queryable filter, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser reportPortalUser, Long launchId) {
		launchAccessValidator.validate(launchId, projectDetails, reportPortalUser);
		return StatisticsConverter.TO_RESOURCE.apply(testItemRepository.accumulateStatisticsByFilter(filter));
	}

	protected void validateProjectRole(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectRole() == OPERATOR, Predicate.isEqual(false)).verify(ACCESS_DENIED);
		}
	}

	private Page<TestItem> getItemsWithLaunchesFiltering(Queryable testItemFilter, Pageable testItemPageable,
			ReportPortalUser.ProjectDetails projectDetails, Long launchFilterId, boolean isLatest, int launchesLimit) {
		Pair<Queryable, Pageable> queryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(projectDetails,
				getShareableEntityHandler.getPermitted(launchFilterId, projectDetails),
				launchesLimit
		);

		return testItemRepository.findByFilter(isLatest,
				queryablePair.getKey(),
				testItemFilter,
				queryablePair.getValue(),
				testItemPageable
		);
	}

	private List<ResourceUpdater<TestItemResource>> getResourceUpdaters(Long projectId, List<TestItem> testItems) {
		return resourceUpdaterProviders.stream()
				.map(retriever -> retriever.retrieve(TestItemUpdaterContent.of(projectId, testItems)))
				.collect(toList());

	}

	@Override
	public List<String> getTicketIds(Long launchId, String term) {
		BusinessRule.expect(term.length() > 2, Predicates.equalTo(true)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", term)
		);
		return ticketRepository.findByLaunchIdAndTerm(launchId, term);
	}

	@Override
	public List<String> getTicketIds(ReportPortalUser.ProjectDetails projectDetails, String term) {
		BusinessRule.expect(term.length() > 0, Predicates.equalTo(true)).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
				Suppliers.formattedSupplier("Length of the filtering string '{}' is less than 1 symbols", term)
		);
		return ticketRepository.findByProjectIdAndTerm(projectDetails.getProjectId(), term);
	}

	@Override
	public List<String> getAttributeKeys(Long launchFilterId, boolean isLatest, int launchesLimit,
			ReportPortalUser.ProjectDetails projectDetails, String keyPart) {
		Pair<Queryable, Pageable> queryablePair = DefaultLaunchFilterProvider.createDefaultLaunchQueryablePair(projectDetails,
				getShareableEntityHandler.getPermitted(launchFilterId, projectDetails),
				launchesLimit
		);
		return itemAttributeRepository.findAllKeysByLaunchFilter(queryablePair.getKey(),
				queryablePair.getValue(),
				isLatest,
				keyPart,
				false
		);
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
		List<ResourceUpdater<TestItemResource>> resourceUpdaters = getResourceUpdaters(projectDetails.getProjectId(), items);
		return items.stream().map(item -> {
			TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(item);
			resourceUpdaters.forEach(updater -> updater.updateResource(testItemResource));
			return testItemResource;
		}).collect(toList());
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
