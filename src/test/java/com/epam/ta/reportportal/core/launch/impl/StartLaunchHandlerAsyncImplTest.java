package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ReportingQueueService;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author Konstantin Antipin
 */

@ExtendWith(MockitoExtension.class)
class StartLaunchHandlerAsyncImplTest {

    @Mock
    AmqpTemplate amqpTemplate;

    @Mock
    ReportingQueueService reportingQueueService;

    @InjectMocks
    StartLaunchHandlerAsyncImpl startLaunchHandlerAsync;

    @Test
    void starLaunch() {
        StartLaunchRQ request = new StartLaunchRQ();
        ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

        startLaunchHandlerAsync.startLaunch(user, user.getProjectDetails().get("test_project"), request);
        verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
        verify(reportingQueueService).getReportingQueueKey(any());
    }
}