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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.launch.LaunchTag;
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

	private static Set<String> getTags(Launch launch) {
		return Optional.ofNullable(launch.getTags()).map(tags -> tags.stream().map(LaunchTag::getValue).collect(toSet())).orElse(null);
	}
}
