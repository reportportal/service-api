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

import com.epam.ta.reportportal.store.database.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.launch.LaunchTag;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.base.Strings;

import java.time.ZoneId;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.store.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.store.commons.EntityUtils.update;
import static com.google.common.collect.Sets.newHashSet;

public class LaunchBuilder implements Supplier<Launch> {

	private Launch launch;

	public LaunchBuilder() {
		this.launch = new Launch();
	}

	public LaunchBuilder addStartRQ(StartLaunchRQ request) {
		if (request != null) {
			launch.setStartTime(request.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			launch.setName(request.getName().trim());
			addDescription(request.getDescription());
			addTags(newHashSet(trimStrings(update(request.getTags()))));
			if (request.getMode() != null) {
				launch.setMode(LaunchModeEnum.valueOf(request.getMode().name()));
			}
		}
		return this;
	}

	public LaunchBuilder addDescription(String description) {
		if (!Strings.isNullOrEmpty(description)) {
			launch.setDescription(description.trim());
		}
		return this;
	}

	public LaunchBuilder addUser(Long userId) {
		launch.setUserId(userId);
		return this;
	}

	public LaunchBuilder addProject(Integer projectId) {
		launch.setProjectId(projectId);
		return this;
	}

	public LaunchBuilder addTags(Set<String> tags) {
		launch.setTags(tags.stream().map(LaunchTag::new).collect(Collectors.toSet()));
		return this;
	}

	@Override
	public Launch get() {
		return launch;
	}
}
