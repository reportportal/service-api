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

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.core.item.impl.history.provider.HistoryProviderFactory;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdater;
import com.epam.ta.reportportal.ws.converter.utils.ResourceUpdaterProvider;
import com.epam.ta.reportportal.ws.converter.utils.item.content.TestItemUpdaterContent;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.google.common.collect.Lists;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_STATS;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_HISTORY_DEPTH_BOUND;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_HISTORY_DEPTH_BOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

/**
 * Creating items history based on {@link TestItem#getTestCaseId()} field
 *
 * @author Pavel Bortnik
 */
@Service
public class TestItemsHistoryHandlerImpl implements TestItemsHistoryHandler {

	@Value("${rp.environment.variable.history.old}")
	private boolean oldHistory;

	private final TestItemRepository testItemRepository;
	private final HistoryProviderFactory historyProviderFactory;
	private final List<ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource>> resourceUpdaterProviders;

	@Autowired
	public TestItemsHistoryHandlerImpl(TestItemRepository testItemRepository, HistoryProviderFactory historyProviderFactory,
			List<ResourceUpdaterProvider<TestItemUpdaterContent, TestItemResource>> resourceUpdaterProviders) {
		this.testItemRepository = testItemRepository;
		this.historyProviderFactory = historyProviderFactory;
		this.resourceUpdaterProviders = resourceUpdaterProviders;
	}

	@Override
	public Iterable<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, Queryable filter,
			Pageable pageable, HistoryRequestParams historyRequestParams, ReportPortalUser user) {

		validateHistoryDepth(historyRequestParams.getHistoryDepth());

		validateProjectRole(projectDetails, user);

		CompositeFilter itemHistoryFilter = new CompositeFilter(Operator.AND,
				filter,
				Filter.builder()
						.withTarget(filter.getTarget().getClazz())
						.withCondition(FilterCondition.builder()
								.eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId()))
								.build())
						.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_MODE, LaunchModeEnum.DEFAULT.name()).build())
						.withCondition(FilterCondition.builder().eq(CRITERIA_HAS_STATS, String.valueOf(Boolean.TRUE)).build())
						.build()
		);

		Page<TestItemHistory> testItemHistoryPage = historyProviderFactory.getProvider(historyRequestParams)
				.orElseThrow(() -> new ReportPortalException(UNABLE_LOAD_TEST_ITEM_HISTORY,
						"Unable to find suitable history baseline provider"
				))
				.provide(itemHistoryFilter, pageable, historyRequestParams, projectDetails, user, !oldHistory);

		return buildHistoryElements(oldHistory ?
						TestItemResource::getUniqueId :
						testItemResource -> String.valueOf(testItemResource.getTestCaseHash()),
				testItemHistoryPage,
				projectDetails.getProjectId(),
				pageable
		);

	}

	private void validateHistoryDepth(int historyDepth) {
		Predicate<Integer> greaterThan = t -> t > MIN_HISTORY_DEPTH_BOUND;
		Predicate<Integer> lessThan = t -> t < MAX_HISTORY_DEPTH_BOUND;
		String historyDepthMessage = Suppliers.formattedSupplier("Items history depth should be greater than '{}' and lower than '{}'",
				MIN_HISTORY_DEPTH_BOUND,
				MAX_HISTORY_DEPTH_BOUND
		).get();
		BusinessRule.expect(historyDepth, greaterThan.and(lessThan)).verify(UNABLE_LOAD_TEST_ITEM_HISTORY, historyDepthMessage);
	}

	private void validateProjectRole(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectRole() == OPERATOR, Predicate.isEqual(false)).verify(ACCESS_DENIED);
		}
	}

	private Iterable<TestItemHistoryElement> buildHistoryElements(Function<TestItemResource, String> groupingFunction,
			Page<TestItemHistory> testItemHistoryPage, Long projectId, Pageable pageable) {

		List<TestItem> testItems = testItemRepository.findAllById(testItemHistoryPage.getContent()
				.stream()
				.flatMap(history -> history.getItemIds().stream())
				.collect(toList()));

		List<ResourceUpdater<TestItemResource>> resourceUpdaters = getResourceUpdaters(projectId, testItems);

		Map<String, Map<Long, TestItemResource>> itemsMapping = testItems.stream().map(item -> {
			TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(item);
			resourceUpdaters.forEach(updater -> updater.updateResource(testItemResource));
			return testItemResource;
		}).collect(groupingBy(groupingFunction, toMap(TestItemResource::getItemId, res -> res)));

		List<TestItemHistoryElement> testItemHistoryElements = testItemHistoryPage.getContent()
				.stream()
				.map(history -> ofNullable(itemsMapping.get(history.getGroupingField())).map(mapping -> {
					TestItemHistoryElement historyResource = new TestItemHistoryElement();
					historyResource.setGroupingField(history.getGroupingField());
					List<TestItemResource> resources = Lists.newArrayList();
					ofNullable(history.getItemIds()).ifPresent(itemIds -> itemIds.forEach(itemId -> ofNullable(mapping.get(itemId)).ifPresent(
							resources::add)));
					historyResource.setResources(resources);
					return historyResource;
				}))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());

		return PagedResourcesAssembler.<TestItemHistoryElement>pageConverter().apply(PageableExecutionUtils.getPage(testItemHistoryElements,
				pageable,
				testItemHistoryPage::getTotalElements
		));

	}

	private List<ResourceUpdater<TestItemResource>> getResourceUpdaters(Long projectId, List<TestItem> testItems) {
		return resourceUpdaterProviders.stream()
				.map(retriever -> retriever.retrieve(TestItemUpdaterContent.of(projectId, testItems)))
				.collect(toList());

	}
}
