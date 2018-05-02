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
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Pavel Bortnik
 */
@Component
public class LaunchReporterConsumer {

	@Autowired
	private DatabaseUserDetailsService userDetailsService;

	@Autowired
	private IStartLaunchHandler startLaunchHandler;

	@RabbitListener(queues = RabbitMqConfiguration.START_REPORTING_QUEUE)
	public void startLaunch(StartLaunchRQ message) {
		ReportPortalUser userDetails = (ReportPortalUser) userDetailsService.loadUserByUsername(message.getUsername());
		startLaunchHandler.startLaunch(userDetails, message.getProjectName(), message);
	}

	@RabbitListener(queues = RabbitMqConfiguration.FINISH_REPORTING_QUEUE)
	public void finishLaunch(FinishExecutionRQ rq) {
		System.out.println(RabbitMqConfiguration.FINISH_REPORTING_QUEUE);
		System.out.println(rq.getEndTime());
	}

}
