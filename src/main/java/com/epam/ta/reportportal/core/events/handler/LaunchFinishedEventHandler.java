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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.subscriber.LaunchFinishedEventSubscriber;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class LaunchFinishedEventHandler {

	private final ProjectRepository projectRepository;
	private final LaunchRepository launchRepository;
	private final List<LaunchFinishedEventSubscriber> launchFinishedEventSubscribers;

	@Autowired
	public LaunchFinishedEventHandler(ProjectRepository projectRepository, LaunchRepository launchRepository,
			List<LaunchFinishedEventSubscriber> launchFinishedEventSubscribers) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.launchFinishedEventSubscribers = launchFinishedEventSubscribers.stream()
				.sorted(Comparator.comparingInt(LaunchFinishedEventSubscriber::getOrder))
				.collect(Collectors.toList());
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void onApplicationEvent(LaunchFinishedEvent event) {
		Launch launch = launchRepository.findById(event.getLaunchActivityResource().getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, event.getLaunchActivityResource().getId()));

		if (LaunchModeEnum.DEBUG == launch.getMode()) {
			return;
		}
		Project project = projectRepository.findById(launch.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, launch.getProjectId()));

		launchFinishedEventSubscribers.forEach(subscriber -> subscriber.handleEvent(event, project, launch));

	}

}
