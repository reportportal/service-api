/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.item.Activity;

/**
 * Builder for {@link com.epam.ta.reportportal.database.entity.item.Activity}
 * persistence layer object
 * 
 * @author Dzmitry_Kavalets
 */
@Service
@Scope("prototype")
public class ActivityBuilder extends Builder<Activity> {

	public ActivityBuilder addUserRef(String userRef) {
		getObject().setUserRef(userRef);
		return this;
	}

	public ActivityBuilder addProjectRef(String projectRef) {
		getObject().setProjectRef(projectRef);
		return this;
	}

	public ActivityBuilder addActionType(String actionType) {
		getObject().setActionType(actionType);
		return this;
	}

	public ActivityBuilder addObjectType(String objectType) {
		getObject().setObjectType(objectType);
		return this;
	}

	public ActivityBuilder addLoggedObjectRef(String loggedObjectRef) {
		getObject().setLoggedObjectRef(loggedObjectRef);
		return this;
	}

	public ActivityBuilder addHistory(Map<String, Activity.FieldValues> history) {
		getObject().setHistory(history);
		return this;
	}

	public ActivityBuilder addObjectName(String name) {
		getObject().setName(name);
		return this;
	}

	@Override
	protected Activity initObject() {
		return new Activity();
	}
}