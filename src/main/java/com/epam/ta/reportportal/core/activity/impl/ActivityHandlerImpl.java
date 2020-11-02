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
package com.epam.ta.reportportal.core.activity.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.activity.ActivityHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ActivityConverter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.*;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author Ihar Kahadouski
 */
@Service
public class ActivityHandlerImpl implements ActivityHandler {

	private final ActivityRepository activityRepository;
	private final TestItemRepository testItemRepository;
	private final LaunchRepository launchRepository;
	private final ProjectRepository projectRepository;

	@Autowired
	public ActivityHandlerImpl(ActivityRepository activityRepository, TestItemRepository testItemRepository,
			LaunchRepository launchRepository, ProjectRepository projectRepository) {
		this.activityRepository = activityRepository;
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public Iterable<ActivityResource> getActivitiesHistory(ReportPortalUser.ProjectDetails projectDetails, Filter filter,
			Queryable predefinedFilter, Pageable pageable) {

		BusinessRule.expect(projectRepository.existsById(projectDetails.getProjectId()), BooleanUtils::isTrue)
				.verify(PROJECT_NOT_FOUND, projectDetails.getProjectId());

		FilterCondition projectCondition = FilterCondition.builder()
				.eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId()))
				.build();
		Page<Activity> page = activityRepository.findByFilter(new CompositeFilter(Operator.AND,
				filter.withCondition(projectCondition),
				predefinedFilter
		), pageable);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(page);
	}

	@Override
	public ActivityResource getActivity(ReportPortalUser.ProjectDetails projectDetails, Long activityId) {
		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ReportPortalException(ACTIVITY_NOT_FOUND, activityId));
		expect(projectDetails.getProjectId(), Predicate.isEqual(activity.getProjectId())).verify(ACCESS_DENIED,
				Suppliers.formattedSupplier("Activity with id '{}' is not under project with id '{}'",
						activityId,
						projectDetails.getProjectId()
				)
		);
		return ActivityConverter.TO_RESOURCE.apply(activity);
	}

	@Override
	public Iterable<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Long itemId, Filter filter,
			Pageable pageable) {
		TestItem testItem = testItemRepository.findById(itemId).orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = launchRepository.findById(testItem.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, testItem.getLaunchId()));
		expect(projectDetails.getProjectId(), Predicate.isEqual(launch.getProjectId())).verify(ACCESS_DENIED,
				Suppliers.formattedSupplier("Test item with id '{}' is not under project with id '{}'",
						itemId,
						projectDetails.getProjectId()
				)
		);

		Sort sortByCreationDateDesc = Sort.by(Sort.Direction.DESC, CRITERIA_CREATION_DATE);

		Filter patternActivityFilter = buildPatternMatchedActivityFilter(filter.getTarget(),
				itemId
		).withConditions(filter.getFilterConditions());

		filter.withCondition(FilterCondition.builder().eq(CRITERIA_OBJECT_ID, String.valueOf(itemId)).build())
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_ENTITY)
						.withCondition(Condition.IN)
						.withValue(Stream.of(ITEM, ITEM_ISSUE, TICKET)
								.map(Activity.ActivityEntityType::getValue)
								.collect(Collectors.joining(",")))
						.build());

		Page<Activity> page = activityRepository.findByFilter(new CompositeFilter(Operator.OR, filter, patternActivityFilter),
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreationDateDesc)
		);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(page);
	}

	@Override
	public Iterable<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		BusinessRule.expect(projectRepository.existsById(projectDetails.getProjectId()), BooleanUtils::isTrue)
				.verify(PROJECT_NOT_FOUND, projectDetails.getProjectId());
		filter.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(projectDetails.getProjectId())).build());
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE)
				.apply(activityRepository.findByFilter(filter, pageable));
	}

	/**
	 * Build {@link Filter} to search for {@link Activity} with {@link Activity.ActivityEntityType#PATTERN} entity
	 * and {@link ActivityAction#PATTERN_MATCHED} action conditions of the {@link TestItem} with provided 'itemId'
	 *
	 * @param filterTarget {@link FilterTarget}
	 * @param itemId       {@link Activity#objectId}
	 * @return {@link Filter} with {@link Activity.ActivityEntityType#PATTERN}, {@link ActivityAction#PATTERN_MATCHED} search conditions
	 */
	private Filter buildPatternMatchedActivityFilter(FilterTarget filterTarget, Long itemId) {
		return Filter.builder()
				.withTarget(filterTarget.getClazz())
				.withCondition(FilterCondition.builder().eq(CRITERIA_OBJECT_ID, String.valueOf(itemId)).build())
				.withCondition(FilterCondition.builder().eq(CRITERIA_ENTITY, Activity.ActivityEntityType.PATTERN.getValue()).build())
				.withCondition(FilterCondition.builder().eq(CRITERIA_ACTION, ActivityAction.PATTERN_MATCHED.getValue()).build())
				.build();
	}
}

