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
import com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;
import com.epam.ta.reportportal.ws.rabbit.MessageHeaders;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.*;
import static java.util.stream.Collectors.toList;

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
    public Launch finishLaunch(String launchId, FinishExecutionRQ request, ReportPortalUser.ProjectDetails projectDetails,
                               ReportPortalUser user) {
        throw new UnsupportedOperationException("Async api unsupported operation");
    }

    @Override
    public FinishLaunchRS finishLaunch(String launchId, FinishExecutionRQ request, ReportPortalUser.ProjectDetails projectDetails,
                                       ReportPortalUser user, LaunchLinkGenerator.LinkParams linkParams) {

        // todo: may be problem - no access to repository, so no possibility to validateRoles() here
        amqpTemplate.convertAndSend(QUEUE_LAUNCH_FINISH, request, message -> {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            headers.put(MessageHeaders.USERNAME, user.getUsername());
            headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
            headers.put(MessageHeaders.LAUNCH_ID, launchId);
            return message;
        });

        FinishLaunchRS response = new FinishLaunchRS();
        return response;
    }

    @Override
    public OperationCompletionRS stopLaunch(String launchId, FinishExecutionRQ request, ReportPortalUser.ProjectDetails projectDetails,
                                            ReportPortalUser user) {

        // todo: may be problem - no access to repository, so no possibility to validateRoles() here
        amqpTemplate.convertAndSend(QUEUE_LAUNCH_STOP, request, message -> {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            headers.put(MessageHeaders.USERNAME, user.getUsername());
            headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
            headers.put(MessageHeaders.LAUNCH_ID, launchId);
            return message;
        });

        OperationCompletionRS response = new OperationCompletionRS("Accepted stop request for launch ID = " + launchId);
        return response;
    }

    @Override
    public List<OperationCompletionRS> stopLaunch(BulkRQ<String, FinishExecutionRQ> request, ReportPortalUser.ProjectDetails projectDetails,
                                                  ReportPortalUser user) {

        // todo: may be problem - no access to repository, so no possibility to validateRoles() here
        amqpTemplate.convertAndSend(QUEUE_LAUNCH_BULK_STOP, request, message -> {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            headers.put(MessageHeaders.USERNAME, user.getUsername());
            headers.put(MessageHeaders.PROJECT_NAME, projectDetails.getProjectName());
            return message;
        });

        return request.getEntities()
                .keySet()
                .stream()
                .map(key -> new OperationCompletionRS("Accepted stop request for launch ID = " + key))
                .collect(toList());
    }

}
