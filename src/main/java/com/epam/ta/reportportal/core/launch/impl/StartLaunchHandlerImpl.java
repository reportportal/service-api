/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.TO_LAUNCH_ATTRIBUTE;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.StartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
@Primary
class StartLaunchHandlerImpl implements com.epam.ta.reportportal.core.launch.StartLaunchHandler {

	private final LaunchRepository launchRepository;
	private final MessageBus messageBus;

	@Autowired
	public StartLaunchHandlerImpl(LaunchRepository launchRepository, MessageBus messageBus) {
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	@Override
	@Transactional
	public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ request) {
		validateRoles(projectDetails, request);

		Launch launch;
		if (BooleanUtils.toBoolean(request.isRerun())) {
			launch = handleRerun(projectDetails, request);
		} else {
			launch = new LaunchBuilder().addStartRQ(request)
					.addAttributes(request.getAttributes())
					.addProject(projectDetails.getProjectId())
					.addUser(user.getUserId())
					.get();
			launchRepository.save(launch);
			launchRepository.refresh(launch);
		}

		messageBus.publishActivity(new LaunchStartedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(), user.getUsername()));

		StartLaunchRS response = new StartLaunchRS();
		response.setId(launch.getId());
		response.setUuid(launch.getUuid());
		response.setNumber(launch.getNumber());
		return response;
	}

	private Launch handleRerun(ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ request) {
		Launch launch;
		Optional<Launch> launchOptional = StringUtils.isEmpty(request.getRerunOf()) ?
				launchRepository.findLatestByNameAndProjectId(request.getName(), projectDetails.getProjectId()) :
				launchRepository.findByUuid(request.getRerunOf());
		launch = launchOptional.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
				ofNullable(request.getRerunOf()).orElse(request.getName())
		));

		ofNullable(request.getMode()).map(it -> LaunchModeEnum.valueOf(it.name())).ifPresent(launch::setMode);
		ofNullable(request.getDescription()).ifPresent(launch::setDescription);
		launch.setStatus(StatusEnum.IN_PROGRESS);
		ofNullable(request.getAttributes()).map(it -> it.stream()
				.map(attr -> TO_LAUNCH_ATTRIBUTE.apply(attr, launch))
				.collect(Collectors.toSet())).ifPresent(launch::setAttributes);
		ofNullable(request.getUuid()).ifPresent(launch::setUuid);
		launch.setRerun(true);
		return launch;
	}
}