package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
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
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
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

    /**
     * Validate {@link ReportPortalUser} credentials. User with a {@link ProjectRole#CUSTOMER} role can't report
     * launches in a debug mode.
     *
     * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
     * @param startLaunchRQ  {@link StartLaunchRQ}
     */
    private void validateRoles(ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
        expect(
                startLaunchRQ.getMode() == Mode.DEBUG && projectDetails.getProjectRole() == ProjectRole.CUSTOMER,
                Predicate.isEqual(false)
        ).verify(ErrorType.FORBIDDEN_OPERATION);
    }

}
