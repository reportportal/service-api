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
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.launch.LaunchTag;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

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
		launch.setUuid(Optional.ofNullable(request.getUuid()).orElse(UUID.randomUUID().toString()));
		addDescription(request.getDescription());
		addTags(request.getTags());
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
		ofNullable(tags).ifPresent(it -> launch.setTags(it.stream()
				.filter(EntityUtils.NOT_EMPTY)
				.map(EntityUtils.REPLACE_SEPARATOR)
				.map(LaunchTag::new)
				.collect(Collectors.toSet())));
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

	public LaunchBuilder addEndTime(Date date) {
		launch.setEndTime(EntityUtils.TO_LOCAL_DATE_TIME.apply(date));
		return this;
	}

	@Override
	public Launch get() {
		return launch;
	}
}
