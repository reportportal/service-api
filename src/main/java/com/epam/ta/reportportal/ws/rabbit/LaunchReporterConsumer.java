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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_FINISH_DLQ_DROPPED;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_START_DLQ_DROPPED;

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

	private AmqpTemplate amqpTemplate;

	@Autowired
	public LaunchReporterConsumer(DatabaseUserDetailsService userDetailsService, StartLaunchHandler startLaunchHandler,
			FinishLaunchHandler finishLaunchHandler, @Qualifier("rabbitTemplate") AmqpTemplate amqpTemplate) {
		this.userDetailsService = userDetailsService;
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
		this.amqpTemplate = amqpTemplate;
	}

	@RabbitListener(queues = "#{ @launchStartQueue.name }")
	public void onStartLaunch(@Payload StartLaunchRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping to {} start request for Launch {}, on maximum retry attempts {}",
						QUEUE_LAUNCH_START_DLQ_DROPPED,
						rq.getUuid(),
						DEAD_LETTER_MAX_RETRY);

				amqpTemplate.convertAndSend(QUEUE_LAUNCH_START_DLQ_DROPPED, rq, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.USERNAME, username);
					headers.put(MessageHeaders.PROJECT_NAME, projectName);
					return message;
				});

				return;
			}
			LOGGER.trace("Retrying start request for Launch {}, attempt {}", rq.getUuid(), count);
		}
		try {
			ReportPortalUser userDetails = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
			startLaunchHandler.startLaunch(userDetails, ProjectExtractor.extractProjectDetails(userDetails, projectName), rq);
		} catch (Exception e) {
			if (e instanceof ReportPortalException) {
				LOGGER.debug("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			} else {
				LOGGER.error("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			}
			throw e;
		}
	}

	@RabbitListener(queues = "#{ @launchFinishQueue.name }")
	public void onFinishLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) String launchId,
			@Header(MessageHeaders.BASE_URL) String baseUrl, @Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping to {} finish request for Launch {}, on maximum retry attempts {}",
						QUEUE_LAUNCH_FINISH_DLQ_DROPPED,
						launchId,
						DEAD_LETTER_MAX_RETRY);

				amqpTemplate.convertAndSend(QUEUE_LAUNCH_FINISH_DLQ_DROPPED, rq, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.USERNAME, username);
					headers.put(MessageHeaders.PROJECT_NAME, projectName);
					headers.put(MessageHeaders.LAUNCH_ID, launchId);
					return message;
				});

				return;
			}
			LOGGER.trace("Retrying finish request for Launch {}, attempt {}", launchId, count);
		}
		try {
			ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
			finishLaunchHandler.finishLaunch(launchId, rq, ProjectExtractor.extractProjectDetails(user, projectName), user, baseUrl);
		} catch (Exception e) {
			if (e instanceof ReportPortalException) {
				LOGGER.debug("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			} else {
				LOGGER.error("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			}
			throw e;
		}
	}

}
