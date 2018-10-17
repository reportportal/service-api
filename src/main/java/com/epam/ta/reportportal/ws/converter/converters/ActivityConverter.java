package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;

import java.util.function.Function;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;

public final class ActivityConverter {

	private ActivityConverter() {
		//static only
	}

	public static final Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
		ActivityResource resource = new ActivityResource();
		resource.setActivityId(activity.getId().toString());
		resource.setLastModifiedDate(TO_DATE.apply(activity.getCreatedAt()));
		resource.setObjectType(activity.getActivityEntityType().name());
		resource.setActionType(activity.getAction());
		resource.setProjectRef(activity.getProjectId().toString());
		resource.setUserRef(activity.getUserId().toString());
		resource.setLoggedObjectRef(activity.getObjectId().toString());
		resource.setDetails(activity.getDetails());
		return resource;
	};
}