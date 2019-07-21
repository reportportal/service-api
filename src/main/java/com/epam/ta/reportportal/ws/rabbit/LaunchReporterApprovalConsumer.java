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

import com.epam.ta.reportportal.core.launch.impl.FinishLaunchApprovalStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
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

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;

/**
 * @author Konstantin Antipin
 *
 * This consumer makes only verify for approve of incoming finish launch requests.
 * The target launch verification is 'no items in progress'.
 * On verified pass message into approved queue for actual processing, otherwise route to retry queue
 *
 */
@Component
@Transactional
public class LaunchReporterApprovalConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LaunchReporterApprovalConsumer.class);

	private LaunchRepository launchRepository;

	private FinishLaunchApprovalStrategy approvalStrategy;

	private AmqpTemplate amqpTemplate;

	@Autowired
	public LaunchReporterApprovalConsumer(LaunchRepository launchRepository,
										  FinishLaunchApprovalStrategy approvalStrategy,
										  @Qualifier("rabbitTemplate") AmqpTemplate amqpTemplate) {
		this.launchRepository = launchRepository;
		this.approvalStrategy = approvalStrategy;
		this.amqpTemplate = amqpTemplate;
	}

	@RabbitListener(queues = "#{ @launchFinishQueue.name }")
	public void onFinishLaunch(@Payload FinishExecutionRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName, @Header(MessageHeaders.LAUNCH_ID) String launchId,
			@Header(MessageHeaders.BASE_URL) String baseUrl, @Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > MESSAGE_MAX_RETRY) {
				LOGGER.error("Dropping to {} finish request for Launch {}, on maximum retry attempts {}",
						QUEUE_LAUNCH_FINISH_DLQ,
						launchId,
						MESSAGE_MAX_RETRY);

				amqpTemplate.convertAndSend(QUEUE_LAUNCH_FINISH_DLQ, rq, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.USERNAME, username);
					headers.put(MessageHeaders.PROJECT_NAME, projectName);
					headers.put(MessageHeaders.LAUNCH_ID, launchId);
					headers.put(MessageHeaders.BASE_URL, baseUrl);
					return message;
				});

				return;
			}
			LOGGER.trace("Retrying finish request for Launch {}, attempt {}", launchId, count);
		}
		try {
			Launch launch = launchRepository.findByUuid(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
			approvalStrategy.verifyNoInProgressItems(launch);

			amqpTemplate.convertAndSend(QUEUE_LAUNCH_FINISH_APPROVED_DELAYED, rq, message -> {
				Map<String, Object> headers = message.getMessageProperties().getHeaders();
				headers.put(MessageHeaders.USERNAME, username);
				headers.put(MessageHeaders.PROJECT_NAME, projectName);
				headers.put(MessageHeaders.LAUNCH_ID, launchId);
				headers.put(MessageHeaders.BASE_URL, baseUrl);
				headers.put(MessageHeaders.XD_HEADER, xdHeader);
				return message;
			});
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
