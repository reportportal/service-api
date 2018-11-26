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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.launch.LaunchTag;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.base.Preconditions;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
public final class LaunchConverter {

	private LaunchConverter() {
		//static only
	}

	public static final Function<Launch, LaunchResource> TO_RESOURCE = db -> {

		Preconditions.checkNotNull(db);

		LaunchResource resource = new LaunchResource();
		resource.setLaunchId(db.getId());
		resource.setName(db.getName());
		resource.setNumber(db.getNumber());
		resource.setDescription(db.getDescription());
		resource.setStatus(db.getStatus() == null ? null : db.getStatus().toString());
		resource.setStartTime(db.getStartTime() == null ? null : EntityUtils.TO_DATE.apply(db.getStartTime()));
		resource.setEndTime(db.getEndTime() == null ? null : EntityUtils.TO_DATE.apply(db.getEndTime()));
		resource.setTags(getTags(db));
		resource.setMode(db.getMode() == null ? null : Mode.valueOf(db.getMode().name()));
		resource.setOwner(db.getUser().getLogin());
		resource.setStatisticsResource(StatisticsConverter.TO_RESOURCE.apply(db.getStatistics()));
		return resource;
	};

	public static final Function<Launch, LaunchActivityResource> TO_ACTIVITY_RESOURCE = launch -> {
		LaunchActivityResource resource = new LaunchActivityResource();
		resource.setId(launch.getId());
		resource.setProjectId(launch.getProjectId());
		resource.setName(launch.getName());
		return resource;
	};

	private static Set<String> getTags(Launch launch) {
		return Optional.ofNullable(launch.getTags()).map(tags -> tags.stream().map(LaunchTag::getValue).collect(toSet())).orElse(null);
	}
}
