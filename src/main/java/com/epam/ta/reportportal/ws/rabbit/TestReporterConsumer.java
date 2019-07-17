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
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_ITEM_FINISH_DLQ_DROPPED;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_ITEM_START_DLQ_DROPPED;

/**
 * @author Pavel Bortnik
 */
@Component
@Transactional
public class TestReporterConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestReporterConsumer.class);

	private DatabaseUserDetailsService userDetailsService;

	private StartTestItemHandler startTestItemHandler;

	private FinishTestItemHandler finishTestItemHandler;

	private AmqpTemplate amqpTemplate;

	@Autowired
	public TestReporterConsumer(DatabaseUserDetailsService userDetailsService, StartTestItemHandler startTestItemHandler,
			FinishTestItemHandler finishTestItemHandler, @Qualifier("rabbitTemplate") AmqpTemplate amqpTemplate) {
		this.userDetailsService = userDetailsService;
		this.startTestItemHandler = startTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
		this.amqpTemplate = amqpTemplate;
	}

	@RabbitListener(queues = "#{ @itemStartQueue.name }")
	public void onItemStart(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
								@Header(name = MessageHeaders.PARENT_ID, required = false) String parentId, @Payload StartTestItemRQ rq,
								@Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping to {} start request for TestItem {}, on maximum retry attempts {}",
						QUEUE_ITEM_START_DLQ_DROPPED,
						rq.getUuid(),
						DEAD_LETTER_MAX_RETRY);

				amqpTemplate.convertAndSend(QUEUE_ITEM_START_DLQ_DROPPED, rq, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.USERNAME, username);
					headers.put(MessageHeaders.PROJECT_NAME, projectName);
					headers.put(MessageHeaders.PARENT_ID, parentId);
					return message;
				});

				return;
			}
			LOGGER.trace("Retrying start request for TestItem {}, attempt {}", rq.getUuid(), count);
		}
		try {
			ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
			ReportPortalUser.ProjectDetails projectDetails = ProjectExtractor.extractProjectDetails(user, normalizeId(projectName));
			if (!Strings.isNullOrEmpty(parentId)) {
				startTestItemHandler.startChildItem(user, projectDetails, rq, parentId);
			} else {
				startTestItemHandler.startRootItem(user, projectDetails, rq);
			}
		} catch (Exception e) {
			if (e instanceof ReportPortalException && e.getMessage().startsWith("Test Item ")) {
				LOGGER.debug("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			} else {
				LOGGER.error("exception : {}, message : {},  cause : {}",
						e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
			}
			throw e;
		}
	}

	@RabbitListener(queues = "#{ @itemFinishQueue.name }")
	public void onFinishItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
							 @Header(MessageHeaders.ITEM_ID) String itemId, @Payload FinishTestItemRQ rq,
							 @Header(required = false, name = MessageHeaders.XD_HEADER) List<Map<String, ?>> xdHeader) {
		if (xdHeader != null) {
			long count = (Long) xdHeader.get(0).get("count");
			if (count > DEAD_LETTER_MAX_RETRY) {
				LOGGER.error("Dropping to {} finish request for TestItem {}, on maximum retry attempts {}",
						QUEUE_ITEM_FINISH_DLQ_DROPPED,
						itemId,
						DEAD_LETTER_MAX_RETRY);

				amqpTemplate.convertAndSend(QUEUE_ITEM_FINISH_DLQ_DROPPED, rq, message -> {
					Map<String, Object> headers = message.getMessageProperties().getHeaders();
					headers.put(MessageHeaders.USERNAME, username);
					headers.put(MessageHeaders.PROJECT_NAME, projectName);
					headers.put(MessageHeaders.ITEM_ID, itemId);
					return message;
				});

				return;
			}
			LOGGER.trace("Retrying finish request for TestItem {}, attempt {}", itemId, count);
		}
		try {
			ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
			finishTestItemHandler.finishTestItem(user, ProjectExtractor.extractProjectDetails(user, normalizeId(projectName)), itemId, rq);
		} catch (Exception e) {
			if (e instanceof ReportPortalException && e.getMessage().startsWith("Test Item ")) {
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
