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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.epam.ta.reportportal.core.configs.RabbitMqConfiguration.QUEUE_START_LAUNCH;

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
    /**
     * @return {@link List < TestItem >}
     */
    public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails,
                                     StartLaunchRQ request) {
        validateRoles(projectDetails, request);

        request.setUuid(UUID.randomUUID().toString());

        amqpTemplate.convertAndSend(QUEUE_START_LAUNCH, request, message -> {
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
