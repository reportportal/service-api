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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.QUEUE_ITEM_START;

/**
 * @author Konstantin Antipin
 */
@Service
@Qualifier("startTestItemHandlerAsync")
class StartTestItemHandlerAsyncImpl implements StartTestItemHandler {

	@Autowired
	@Qualifier(value = "rabbitTemplate")
	AmqpTemplate amqpTemplate;

	@Override
	public ItemCreatedRS startRootItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ request) {

		// todo: may be problem - no access to repository, so no possibility to validateRoles() here
		amqpTemplate.convertAndSend(QUEUE_ITEM_START, request, message -> {
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			headers.put(MessageHeaders.USERNAME, user.getUsername());
			headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
			headers.put(MessageHeaders.PARENT_ID, "");
			return message;
		});

		ItemCreatedRS response = new ItemCreatedRS();
		response.setUuid(Optional.ofNullable(request.getUuid()).orElse(UUID.randomUUID().toString()));
		return response;
	}

	@Override
	public ItemCreatedRS startChildItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ request,
			String parentId) {

		// todo: may be problem - no access to repository, so no possibility to validateRoles() here
		amqpTemplate.convertAndSend(QUEUE_ITEM_START, request, message -> {
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			headers.put(MessageHeaders.USERNAME, user.getUsername());
			headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
			headers.put(MessageHeaders.PARENT_ID, parentId);
			return message;
		});

		ItemCreatedRS response = new ItemCreatedRS();
		response.setUuid(Optional.ofNullable(request.getUuid()).orElse(UUID.randomUUID().toString()));
		return response;
	}
}
