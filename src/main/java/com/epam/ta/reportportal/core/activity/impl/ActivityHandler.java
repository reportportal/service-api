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

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

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
		expect(projectRepository.exists(projectName), equalTo(true)).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		return activityRepository.findActivitiesByProjectId(projectName, filter, pageable).stream()
				.map(activity -> activityResourceAssembler.toResource(activity)).collect(Collectors.toList());
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
	public List<ActivityResource> getItemActivities(String projectName, String itemId, Filter filter, Pageable pageable) {
		TestItem testItem = testItemRepository.findOne(itemId);
		expect(testItem, notNull()).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		String projectRef = launchRepository.findOne(testItem.getLaunchRef()).getProjectRef();
		expect(projectName, equalTo(projectRef)).verify(ErrorType.TEST_ITEM_NOT_FOUND, itemId);
		return activityRepository.findActivitiesByTestItemId(itemId, filter, pageable).stream()
				.map(activity -> activityResourceAssembler.toResource(activity)).collect(Collectors.toList());
	}
}
