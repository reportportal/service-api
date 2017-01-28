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

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;

/**
 * Builder for {@link com.epam.ta.reportportal.ws.model.ActivityResource} domain
 * model object
 */
@Service
public class ActivityResourceBuilder extends Builder<ActivityResource> {

	public ActivityResourceBuilder addActivity(Activity activity) {
		if (null != activity) {

			getObject().setUserRef(activity.getUserRef());
			getObject().setActivityId(activity.getId());
			getObject().setLoggedObjectRef(activity.getLoggedObjectRef());
			getObject().setLastModifiedDate(activity.getLastModified());
			getObject().setObjectType(activity.getObjectType());
			getObject().setActionType(activity.getActionType());

			if ((null != activity.getHistory()) || (!activity.getHistory().isEmpty()) ) {
				Map<String, ActivityResource.FieldValues> resourceHistory = new HashMap<>();
				Map<String, Activity.FieldValues> history = activity.getHistory();

				for (Map.Entry<String,Activity.FieldValues> entry : history.entrySet()) {
					ActivityResource.FieldValues fieldValues = new ActivityResource.FieldValues();
					fieldValues.setOldValue(entry.getValue().getOldValue());
					fieldValues.setNewValue(entry.getValue().getNewValue());
					resourceHistory.put(entry.getKey(), fieldValues);
				}

				getObject().setHistory(resourceHistory);
			}
		}
		return this;
	}

	@Override
	protected ActivityResource initObject() {
		return new ActivityResource();
	}
}
