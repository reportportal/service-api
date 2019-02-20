/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pavel Bortnik
 */
@Component
@Transactional
public class LaunchReporterConsumer {

	private DatabaseUserDetailsService userDetailsService;

	private StartLaunchHandler startLaunchHandler;

	private FinishLaunchHandler finishLaunchHandler;

	@Autowired
	public LaunchReporterConsumer(DatabaseUserDetailsService userDetailsService, StartLaunchHandler startLaunchHandler,
			FinishLaunchHandler finishLaunchHandler) {
		this.userDetailsService = userDetailsService;
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
	}

	@RabbitListener(queues = "#{ @startLaunchQueue.name }")
	public void onStartLaunch(@Payload StartLaunchRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName) {
		ReportPortalUser userDetails = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		startLaunchHandler.startLaunch(userDetails, ProjectExtractor.extractProjectDetails(userDetails, projectName), rq);
	}

	@RabbitListener(queues = "#{ @finishLaunchQueue.name }")
	public void onFinishLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) Long launchId) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.finishLaunch(launchId, rq, ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

}
