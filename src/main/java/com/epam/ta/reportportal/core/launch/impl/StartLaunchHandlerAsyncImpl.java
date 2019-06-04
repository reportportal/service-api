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
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_START;

/**
 * @author Konstantin Antipin
 */
@Service
@Qualifier("startLaunchHandlerAsync")
public class StartLaunchHandlerAsyncImpl implements StartLaunchHandler {

    @Autowired
    @Qualifier(value = "rabbitTemplate")
    AmqpTemplate amqpTemplate;

    @Override
    public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails,
                                     StartLaunchRQ request) {
        validateRoles(projectDetails, request);

        request.setUuid(UUID.randomUUID().toString());
        amqpTemplate.convertAndSend(QUEUE_LAUNCH_START, request, message -> {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            headers.put(MessageHeaders.USERNAME, user.getUsername());
            headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
            return message;
        });

        StartLaunchRS response = new StartLaunchRS();
        response.setUuid(request.getUuid());
        return response;
    }
}
