package com.epam.ta.reportportal.core.dashboard.impl;

import static org.junit.Assert.*;

import com.epam.ta.reportportal.auth.acl.ReportPortalAclService;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.DashboardRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateDashboardHandlerTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private MessageBus messageBus;

    @Mock
    private ReportPortalAclService aclService;

    @InjectMocks
    private CreateDashboardHandler createDashboardHandler;

    @Test
    public void testMockInjected() {
        assertEquals(dashboardRepository, createDashboardHandler.getDashboardRepository());
        assertEquals(messageBus, createDashboardHandler.getMessageBus());
        assertEquals(aclService, createDashboardHandler.getAclService());
    }
}