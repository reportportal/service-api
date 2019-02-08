/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.model.ActivityResource;

import java.util.function.Function;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class ActivityConverter {

	private ActivityConverter() {
		//static only
	}

	public static final Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
		ActivityResource resource = new ActivityResource();
		resource.setId(activity.getId());
		resource.setLastModified(TO_DATE.apply(activity.getCreatedAt()));
		resource.setObjectType(activity.getActivityEntityType().getValue());
		resource.setActionType(activity.getAction());
		resource.setProjectId(activity.getProjectId());
		resource.setUser(activity.getUsername());
		ofNullable(activity.getObjectId()).ifPresent(resource::setLoggedObjectId);
		resource.setDetails(activity.getDetails());
		return resource;
	};
}