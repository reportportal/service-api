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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class LaunchBuilder implements Supplier<Launch> {

	private static final int LAUNCH_DESCRIPTION_LENGTH_LIMIT = 1024;
	private static final int DESCRIPTION_START_SYMBOL_INDEX = 0;

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
		addAttributes(request.getAttributes());
		ofNullable(request.getMode()).ifPresent(it -> launch.setMode(LaunchModeEnum.valueOf(request.getMode().name())));
		return this;
	}

	public LaunchBuilder addDescription(String description) {
		//launch description length limitation is 1024 symbols
		ofNullable(description).ifPresent(it -> launch.setDescription(StringUtils.substring(it.trim(),
				DESCRIPTION_START_SYMBOL_INDEX,
				LAUNCH_DESCRIPTION_LENGTH_LIMIT
		)));
		return this;
	}

	public LaunchBuilder addUser(Long userId) {
		User user = new User();
		user.setId(userId);
		launch.setUser(user);
		return this;
	}

	public LaunchBuilder addProject(Long projectId) {
		launch.setProjectId(projectId);
		return this;
	}

	public LaunchBuilder addAttribute(ItemAttributeResource attributeResource) {
		ItemAttribute itemAttribute = new ItemAttribute();
		itemAttribute.setKey(attributeResource.getKey());
		itemAttribute.setValue(attributeResource.getValue());
		itemAttribute.setSystem(attributeResource.isSystem());
		itemAttribute.setLaunch(launch);
		launch.getAttributes().add(itemAttribute);
		return this;
	}

	public LaunchBuilder addAttributes(Set<ItemAttributeResource> attributes) {
		ofNullable(attributes).ifPresent(it -> launch.getAttributes().addAll(it.stream().map(val -> {
			ItemAttribute itemAttribute = new ItemAttribute();
			itemAttribute.setValue(val.getValue());
			itemAttribute.setKey(val.getKey());
			itemAttribute.setSystem(val.isSystem());
			itemAttribute.setLaunch(launch);
			return itemAttribute;
		}).collect(Collectors.toSet())));
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
