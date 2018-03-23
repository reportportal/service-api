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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.launch.LaunchTag;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.store.commons.EntityUtils.trimStrings;
import static com.epam.ta.reportportal.store.commons.EntityUtils.update;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.StreamSupport.stream;

public class LaunchBuilder implements Supplier<Launch> {

	private Launch launch;

	public LaunchBuilder() {
		this.launch = new Launch();
	}

	public LaunchBuilder(Launch launch) {
		this.launch = launch;
	}

	public LaunchBuilder addStartRQ(StartLaunchRQ request) {
		Preconditions.checkNotNull(request, ErrorType.BAD_REQUEST_ERROR);
		launch.setStartTime(EntityUtils.TO_LOCAL_DATE_TIME.apply(request.getStartTime()));
		launch.setName(request.getName().trim());
		launch.setStatus(StatusEnum.IN_PROGRESS);
		addDescription(request.getDescription());
		addTags(newHashSet(trimStrings(update(request.getTags()))));
		ofNullable(request.getMode()).ifPresent(it -> launch.setMode(LaunchModeEnum.valueOf(request.getMode().name())));
		return this;
	}

	public LaunchBuilder addDescription(String description) {
		ofNullable(description).ifPresent(it -> launch.setDescription(it.trim()));
		return this;
	}

	public LaunchBuilder addUser(Long userId) {
		launch.setUserId(userId);
		return this;
	}

	public LaunchBuilder addProject(Long projectId) {
		launch.setProjectId(projectId);
		return this;
	}

	public LaunchBuilder addTag(String tag) {
		Preconditions.checkNotNull(tag, "Provided value should not be null");
		Set<LaunchTag> newTags = Sets.newHashSet(launch.getTags());
		newTags.add(new LaunchTag(tag));
		launch.setTags(newTags);
		return this;
	}

	public LaunchBuilder addTags(Set<String> tags) {
		ofNullable(tags).ifPresent(it -> launch.setTags(
				stream((trimStrings(update(it)).spliterator()), false).map(LaunchTag::new).collect(Collectors.toSet())));
		return this;
	}

	public LaunchBuilder addMode(Mode mode) {
		ofNullable(mode).ifPresent(it -> launch.setMode(LaunchModeEnum.valueOf(it.name())));
		return this;
	}

	public LaunchBuilder addStatus(String status) {
		launch.setStatus(StatusEnum.fromValue(status).orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FINISH_STATUS)));
		return this;
	}

	@Override
	public Launch get() {
		return launch;
	}
}
