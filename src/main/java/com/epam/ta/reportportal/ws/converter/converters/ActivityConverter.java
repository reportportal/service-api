package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;

import java.sql.Date;
import java.time.ZoneId;
import java.util.function.Function;

public final class ActivityConverter {

	private ActivityConverter() {
		//static only
	}

	public static final Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
		ActivityResource resource = new ActivityResource();
		resource.setActivityId(activity.getId().toString());
		resource.setLastModifiedDate(Date.from(activity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
		resource.setObjectType(activity.getEntity().name());
		resource.setActionType(activity.getAction());
		resource.setProjectRef(activity.getProjectId().toString());
		resource.setUserRef(activity.getUserId().toString());
		resource.setHistory(activity.getDetails());
		return resource;
	};
}