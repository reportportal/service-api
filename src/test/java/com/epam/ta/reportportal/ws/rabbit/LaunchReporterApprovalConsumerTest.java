package com.epam.ta.reportportal.ws.rabbit;

import com.epam.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.impl.FinishLaunchApprovalStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.Collections;
import java.util.Date;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.MESSAGE_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_FINISH_DLQ;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Konstantin Antipin
 */
class LaunchReporterApprovalConsumerTest {

    @Mock
    LaunchRepository launchRepository;

    @Mock
    private FinishLaunchApprovalStrategy approvalStrategy;

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private LaunchReporterApprovalConsumer launchReporterApprovalConsumer;

    @Test
    void onFinishLaunch() {
        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
        finishExecutionRQ.setEndTime(new Date());
        finishExecutionRQ.setDescription("description");
        String launchId = "1";

        doThrow(new ReportPortalException(1, "message", null)).when(approvalStrategy).verifyNoInProgressItems(any(Launch.class));

        launchReporterApprovalConsumer.onFinishLaunch(finishExecutionRQ, "user", "test_project", launchId, "http://example.com", null);

        verify(launchRepository).findByUuid(eq(launchId));
        verify(approvalStrategy).verifyNoInProgressItems(any(Launch.class));
        verify(amqpTemplate, never()).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));
    }

    @Test
    void onFinishLaunchSpooledToRetry() {
        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
        finishExecutionRQ.setEndTime(new Date());
        finishExecutionRQ.setDescription("description");
        String launchId = "1";

        launchReporterApprovalConsumer.onFinishLaunch(finishExecutionRQ, "user", "test_project", launchId, "http://example.com", null);

        verify(launchRepository).findByUuid(eq(launchId));
        verify(approvalStrategy).verifyNoInProgressItems(any(Launch.class));
        verify(amqpTemplate).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));
    }

}