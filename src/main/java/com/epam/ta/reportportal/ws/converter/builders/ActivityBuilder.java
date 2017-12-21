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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.entity.item.ActivityObjectType;

import java.util.List;
import java.util.function.Supplier;

/**
 * Builder for {@link com.epam.ta.reportportal.database.entity.item.Activity}
 * persistence layer object
 *
 * @author Dzmitry_Kavalets
 */
public class ActivityBuilder implements Supplier<Activity> {

	private Activity activity;

	public ActivityBuilder() {
		activity = new Activity();
	}

	public ActivityBuilder addUserRef(String userRef) {
		activity.setUserRef(userRef);
		return this;
	}

	public ActivityBuilder addProjectRef(String projectRef) {
		activity.setProjectRef(projectRef);
		return this;
	}

	public ActivityBuilder addActionType(ActivityEventType actionType) {
		activity.setActionType(actionType);
		return this;
	}

	public ActivityBuilder addObjectType(ActivityObjectType objectType) {
		activity.setObjectType(objectType);
		return this;
	}

	public ActivityBuilder addLoggedObjectRef(String loggedObjectRef) {
		activity.setLoggedObjectRef(loggedObjectRef);
		return this;
	}

	public ActivityBuilder addHistory(List<Activity.FieldValues> history) {
		activity.setHistory(history);
		return this;
	}

	public ActivityBuilder addObjectName(String name) {
		activity.setName(name);
		return this;
	}

	@Override
	public Activity get() {
		return activity;
	}
}