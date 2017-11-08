/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.activity.impl;

import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.converter.ActivityResourceAssembler;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.database.entity.item.Activity.PROJECT_REF;
import static com.epam.ta.reportportal.database.search.Condition.EQUALS;

/**
 * @author Dzmitry_Kavalets
 * @author Andrei Varabyeu
 */
@Service
public class ActivityHandler implements IActivityHandler {

	private final ActivityRepository activityRepository;
	private final TestItemRepository testItemRepository;
	private final LaunchRepository launchRepository;
	private final ProjectRepository projectRepository;
	private final ActivityResourceAssembler activityResourceAssembler;

	@Autowired
	public ActivityHandler(ActivityRepository activityRepository, TestItemRepository testItemRepository, LaunchRepository launchRepository,
			ProjectRepository projectRepository, ActivityResourceAssembler activityResourceAssembler) {
		this.activityRepository = activityRepository;
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
		this.activityResourceAssembler = activityResourceAssembler;
	}

	@Override
	public List<ActivityResource> getActivitiesHistory(String projectName, Filter filter, Pageable pageable) {
		expect(projectRepository.exists(projectName), equalTo(true)).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		return activityRepository.findActivitiesByProjectId(projectName, filter, pageable)
				.stream()
				.map(activityResourceAssembler::toResource)
				.collect(Collectors.toList());
	}

	@Override
	public ActivityResource getActivity(String projectName, String activityId) {
		expect(activityId, notNull()).verify(ErrorType.ACTIVITY_NOT_FOUND, activityId);
		Activity activity = activityRepository.findOne(activityId);
		expect(activity, notNull()).verify(ErrorType.ACTIVITY_NOT_FOUND, activityId);
		expect(projectName, equalTo(activity.getProjectRef())).verify(ErrorType.TEST_ITEM_NOT_FOUND, activityId);
		return activityResourceAssembler.toResource(activity);
	}

	@Override
	public com.epam.ta.reportportal.ws.model.Page<ActivityResource> getItemActivities(String projectName, Filter filter,
			Pageable pageable) {
		expect(projectRepository.exists(projectName), equalTo(true)).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		filter.addCondition(new FilterCondition(EQUALS, false, projectName, PROJECT_REF));
		Page<Activity> page = activityRepository.findByFilter(filter, pageable);
		return activityResourceAssembler.toPagedResources(page);
	}

	@Override
	public List<ActivityResource> getItemActivities(String projectName, String itemId, Filter filter, Pageable pageable) {
		TestItem testItem = testItemRepository.findOne(itemId);
		expect(testItem, notNull()).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		String projectRef = launchRepository.findOne(testItem.getLaunchRef()).getProjectRef();
		expect(projectName, equalTo(projectRef)).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		return activityRepository.findActivitiesByTestItemId(itemId, filter, pageable)
				.stream()
				.map(activityResourceAssembler::toResource)
				.collect(Collectors.toList());
	}
}
