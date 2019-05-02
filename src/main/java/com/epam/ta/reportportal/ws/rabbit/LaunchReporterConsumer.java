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
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;

/**
 * @author Pavel Bortnik
 */
@Component
@Transactional
public class LaunchReporterConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchReporterConsumer.class);

	private DatabaseUserDetailsService userDetailsService;

	private StartLaunchHandler startLaunchHandler;

	private FinishLaunchHandler finishLaunchHandler;

	@Autowired
	public LaunchReporterConsumer(DatabaseUserDetailsService userDetailsService,
								  StartLaunchHandler startLaunchHandler,
								  FinishLaunchHandler finishLaunchHandler) {
		this.userDetailsService = userDetailsService;
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
	}

	@RabbitListener(queues = "#{ @launchStartQueue.name }")
	public void onStartLaunch(@Payload StartLaunchRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName) {
		ReportPortalUser userDetails = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		startLaunchHandler.startLaunch(userDetails, ProjectExtractor.extractProjectDetails(userDetails, projectName), rq);
	}

	@RabbitListener(queues = "#{ @launchFinishQueue.name }")
	public void onFinishLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) String launchId,
			@Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping finish request for Launch {}, on maximum retry attempts {}", launchId, DEAD_LETTER_MAX_RETRY);
				return;
			}
			LOGGER.warn("Retrying finish request  for Launch {}, attempt {}", launchId, count);
		}
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.finishLaunch(launchId, rq, ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

	@RabbitListener(queues = "#{ @launchStopQueue.name }")
	public void onStopLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
							   @Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) String launchId,
							   @Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping stop request for Launch {}, on maximum retry attempts {}", launchId, DEAD_LETTER_MAX_RETRY);
				return;
			}
			LOGGER.warn("Retrying stop request  for Launch {}, attempt {}", launchId, count);
		}
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.stopLaunch(launchId, rq, ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

	@RabbitListener(queues = "#{ @launchBulkStopQueue.name }")
	public void onBulkStopLaunch(@Payload BulkRQ<String, FinishExecutionRQ> rq, @Header(MessageHeaders.USERNAME) String username,
								 @Header(MessageHeaders.PROJECT_NAME) String projectName,
								 @Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		/*
		 * don't do any processing before xdHeader check, to safeguard from DLQ infinite cycling if any
		 * occasional exception happens. So we omit preparing launchId(s) from rq for LOGGER
		 */
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping bulk stop request,on maximum retry attempts {}", DEAD_LETTER_MAX_RETRY);
				return;
			}
			LOGGER.warn("Retrying bulk stop request, attempt {}", count);
		}
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishLaunchHandler.stopLaunch(rq, ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

}
