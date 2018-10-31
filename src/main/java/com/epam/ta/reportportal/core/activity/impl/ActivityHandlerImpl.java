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
package com.epam.ta.reportportal.core.activity.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.activity.ActivityHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ActivityConverter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author Ihar Kahadouski
 */
@Service
public class ActivityHandlerImpl implements ActivityHandler {

	private static final String CREATION_DATE_COLUMN = "creation_date";

	private final ActivityRepository activityRepository;
	private final TestItemRepository testItemRepository;
	private final ProjectRepository projectRepository;

	@Autowired
	public ActivityHandlerImpl(ActivityRepository activityRepository, TestItemRepository testItemRepository,
			ProjectRepository projectRepository) {
		this.activityRepository = activityRepository;
		this.testItemRepository = testItemRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public Iterable<ActivityResource> getActivitiesHistory(ReportPortalUser.ProjectDetails projectDetails, Filter filter,
			Pageable pageable) {
		projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		FilterCondition projectCondition = new FilterCondition(Condition.EQUALS,
				false,
				projectDetails.getProjectId().toString(),
				CRITERIA_PROJECT_ID
		);

		org.springframework.data.domain.Page<Activity> page = activityRepository.findByFilter(filter.withCondition(projectCondition),
				pageable
		);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(page).getContent();
	}

	@Override
	public ActivityResource getActivity(ReportPortalUser.ProjectDetails projectDetails, Long activityId) {
		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ReportPortalException(ACTIVITY_NOT_FOUND, activityId));
		expect(projectDetails.getProjectId(), Predicate.isEqual(activity.getProjectId())).verify(ACCESS_DENIED, activityId);
		return ActivityConverter.TO_RESOURCE.apply(activity);
	}

	@Override
	public Iterable<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Long itemId, Filter filter,
			Pageable pageable) {
		TestItem testItem = testItemRepository.findById(itemId).orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = testItem.getLaunch();
		expect(projectDetails.getProjectId(), Predicate.isEqual(launch.getProjectId())).verify(ACCESS_DENIED, itemId);

		Sort sortByCreationDateDesc = new Sort(Sort.Direction.DESC, CREATION_DATE_COLUMN);
		FilterCondition testItemCondition = new FilterCondition(Condition.EQUALS, false, itemId.toString(), CRITERIA_OBJECT_ID);

		org.springframework.data.domain.Page<Activity> page = activityRepository.findByFilter(filter.withCondition(testItemCondition),
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreationDateDesc)
		);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(page).getContent();
	}

	@Override
	public Page<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		org.springframework.data.domain.Page<Activity> activityPage = activityRepository.findByFilter(filter.withCondition(new FilterCondition(Condition.EQUALS,
				false,
				projectDetails.getProjectId().toString(),
				CRITERIA_PROJECT_ID
		)), pageable);
		return PagedResourcesAssembler.pageConverter(ActivityConverter.TO_RESOURCE).apply(activityPage);
	}
}

