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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
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
		startLaunchHandler.startLaunch(userDetails, ProjectUtils.extractProjectDetails(userDetails, projectName), rq);
	}

	@RabbitListener(queues = "#{ @finishLaunchQueue.name }")
	public void onFinishLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) Long launchId) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.finishLaunch(launchId, rq, ProjectUtils.extractProjectDetails(user, projectName), user);
	}

}
