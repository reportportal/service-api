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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import com.epam.ta.reportportal.ws.rabbit.RequestType;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_ITEM_FINISH;

/**
 * @author Konstantin Antipin
 */
@Service
@Qualifier("finishTestItemHandlerAsync")
public class FinishTestItemHandlerAsyncImpl implements FinishTestItemHandler {

	@Autowired
	@Qualifier(value = "rabbitTemplate")
	AmqpTemplate amqpTemplate;

	@Override
	public OperationCompletionRS finishTestItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, String testItemId,
			FinishTestItemRQ request) {

		// todo: may be problem - no access to repository, so no possibility to validateRoles() here
		amqpTemplate.convertAndSend(QUEUE_ITEM_FINISH, request, message -> {
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			headers.put(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_TEST);
			headers.put(MessageHeaders.USERNAME, user.getUsername());
			headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
			headers.put(MessageHeaders.ITEM_ID, testItemId);
			return message;
		});

		OperationCompletionRS response = new OperationCompletionRS("Accepted finish request for test item ID = " + testItemId);
		return response;

	}
}
