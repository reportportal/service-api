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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.activity.IActivityHandler;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.converter.ActivityResourceAssembler;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.ErrorType;

/**
 * @author Dzmitry_Kavalets
 */
@Service
public class ActivityHandler implements IActivityHandler {

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ActivityResourceAssembler activityResourceAssembler;

	@Override
	public List<ActivityResource> getActivitiesHistory(String projectName, Filter filter, Pageable pageable) {
		Project project = projectRepository.findOne(projectName);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		return activityRepository.findActivitiesByProjectId(projectName, filter, pageable).stream()
				.map(activity -> activityResourceAssembler.toResource(activity, projectName)).collect(Collectors.toList());
	}

	@Override
	public ActivityResource getActivity(String projectName, String activityId) {
		BusinessRule.expect(activityId, Predicates.notNull()).verify(ErrorType.ACTIVITY_NOT_FOUND, activityId);
		Activity activity = activityRepository.findOne(activityId);
		BusinessRule.expect(activity, Predicates.notNull()).verify(ErrorType.ACTIVITY_NOT_FOUND, activityId);
		BusinessRule.expect(projectName, Predicates.equalTo(activity.getProjectRef())).verify(ErrorType.TEST_ITEM_NOT_FOUND, activityId);
		return activityResourceAssembler.toResource(activity);
	}

	@Override
	public List<ActivityResource> getItemActivities(String projectName, String itemId, Filter filter, Pageable pageable) {
		TestItem testItem = testItemRepository.findOne(itemId);
		BusinessRule.expect(testItem, Predicates.notNull()).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		String projectRef = launchRepository.findOne(testItem.getLaunchRef()).getProjectRef();
		BusinessRule.expect(projectName, Predicates.equalTo(projectRef)).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		return activityRepository.findActivitiesByTestItemId(itemId, filter, pageable).stream()
				.map(activity -> activityResourceAssembler.toResource(activity, projectName)).collect(Collectors.toList());
	}
}