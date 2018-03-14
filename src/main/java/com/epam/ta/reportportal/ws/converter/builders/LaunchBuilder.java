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

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.commons.EntityUtils.update;

public class LaunchBuilder implements Supplier<Launch> {

	private Launch launch;

	public LaunchBuilder() {
		this.launch = new Launch();
	}

	public LaunchBuilder addStartRQ(StartLaunchRQ request) {
		if (request != null) {
			launch.setStartTime(Optional.ofNullable(request.getStartTime()).orElse(Date.from(Instant.now())));
			launch.setName(request.getName().trim());
			addDescription(request.getDescription());
			addTags(request.getTags());
			if (request.getMode() != null) {
				launch.setMode(request.getMode());
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

	public LaunchBuilder addTags(Set<String> tags) {
		if (!CollectionUtils.isEmpty(tags)) {
			Set<String> trimmedTags = Sets.newHashSet(trimStrings(update(tags)));
			launch.setTags(trimmedTags);
		}
		return this;
	}

	public LaunchBuilder addStatus(Status status) {
		launch.setStatus(status);
		return this;
	}

	public LaunchBuilder addUser(String userName) {
		launch.setUserRef(userName);
		return this;
	}

	public LaunchBuilder addProject(String projectName) {
		launch.setProjectRef(projectName);
		return this;
	}

	public LaunchBuilder addEndTime(Date endTime) {
		launch.setEndTime(endTime);
		return this;
	}

	@Override
	public Launch get() {
		return launch;
	}
}
