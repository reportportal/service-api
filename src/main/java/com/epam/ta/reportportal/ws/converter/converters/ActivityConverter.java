/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ActivityConverter {

	private ActivityConverter() {
		//static only
	}

	public final static Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
		Preconditions.checkNotNull(activity);
		ActivityResource resource = new ActivityResource();
		resource.setUserRef(activity.getUserRef());
		resource.setProjectRef(activity.getProjectRef());
		resource.setActivityId(activity.getId());
		resource.setLoggedObjectRef(activity.getLoggedObjectRef());
		resource.setLastModifiedDate(activity.getLastModified());
		resource.setObjectType(activity.getObjectType().getValue());
		resource.setActionType(activity.getActionType().getValue());
		resource.setObjectName(activity.getName());
		List<ActivityResource.FieldValues> history = Optional.ofNullable(activity.getHistory())
				.orElseGet(Collections::emptyList)
				.stream()
				.map(ActivityConverter.TO_FIELD_RESOURCE)
				.collect(Collectors.toList());
		resource.setHistory(history);
		return resource;

	};

	private static final Function<Activity.FieldValues, ActivityResource.FieldValues> TO_FIELD_RESOURCE = db -> {
		ActivityResource.FieldValues fieldValues = new ActivityResource.FieldValues();
		fieldValues.setField(db.getField());
		fieldValues.setOldValue(db.getOldValue());
		fieldValues.setNewValue(db.getNewValue());
		return fieldValues;
	};

}
