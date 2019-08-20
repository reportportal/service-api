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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import com.epam.ta.reportportal.ws.rabbit.RequestType;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.EXCHANGE_REPORTING;
import static com.epam.ta.reportportal.util.ControllerUtils.getReportingQueueKey;

/**
 * @author Konstantin Antipin
 */
@Service
@Qualifier("finishLaunchHandlerAsync")
public class FinishLaunchHandlerAsyncImpl implements FinishLaunchHandler {

	@Autowired
	@Qualifier(value = "rabbitTemplate")
	AmqpTemplate amqpTemplate;

	@Override
	public FinishLaunchRS finishLaunch(String launchId, FinishExecutionRQ request, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, String baseUrl) {

		// todo: may be problem - no access to repository, so no possibility to validateRoles() here
		amqpTemplate.convertAndSend(EXCHANGE_REPORTING, getReportingQueueKey(launchId), request, message -> {
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			headers.put(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_LAUNCH);
			headers.put(MessageHeaders.USERNAME, user.getUsername());
			headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
			headers.put(MessageHeaders.LAUNCH_ID, launchId);
			headers.put(MessageHeaders.BASE_URL, baseUrl);
			return message;
		});

		FinishLaunchRS response = new FinishLaunchRS();
		response.setId(launchId);
		return response;
	}
}
