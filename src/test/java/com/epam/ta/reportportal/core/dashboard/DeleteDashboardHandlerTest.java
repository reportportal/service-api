package com.epam.ta.reportportal.core.dashboard;

import com.epam.ta.reportportal.core.dashboard.impl.DeleteDashboardHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteDashboardHandlerTest {

    private static final String DASHBOARD = "dashboard";
    private static final String OWNER = "owner";
    private static final String PROJECT = "project";

    private final DashboardRepository dashboardRepository = mock(DashboardRepository.class);

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

    private DeleteDashboardHandler handler =
            new DeleteDashboardHandler(dashboardRepository, projectRepository);

    @Test
    public void deleteDashboard() throws Exception {
        when(dashboardRepository.findOne(DASHBOARD)).thenReturn(getOwnerDashboard());
        when(projectRepository.findProjectRoles(OWNER)).thenReturn(getProjectRoles());
        OperationCompletionRS rs = handler.deleteDashboard(DASHBOARD, OWNER, PROJECT, UserRole.USER);
        assertTrue(rs.getResultMessage().contains(DASHBOARD));
    }

    @Test(expected = ReportPortalException.class)
    public void negativeDeleteDashboard() {
        when(dashboardRepository.findOne(DASHBOARD)).thenReturn(getNotOwnerDashboard());
        when(projectRepository.findProjectRoles(OWNER)).thenReturn(getProjectRoles());
        handler.deleteDashboard(DASHBOARD, OWNER, PROJECT, UserRole.USER);
    }

    @Test
    public void deleteFilterByPm() {
        when(dashboardRepository.findOne(DASHBOARD)).thenReturn(getNotOwnerDashboard());
        when(projectRepository.findProjectRoles(OWNER)).thenReturn(getProjectRolesForPM());
        OperationCompletionRS operationCompletionRS = handler.deleteDashboard(DASHBOARD, OWNER, PROJECT, UserRole.USER);
        assertTrue(operationCompletionRS.getResultMessage().contains(DASHBOARD));
    }

    @Test
    public void deleteFilterByAdmin() {
        when(dashboardRepository.findOne(DASHBOARD)).thenReturn(getNotOwnerDashboard());
        when(projectRepository.findProjectRoles(OWNER)).thenReturn(getProjectRoles());
        OperationCompletionRS rs = handler.deleteDashboard(DASHBOARD, OWNER, PROJECT, UserRole.ADMINISTRATOR);
        assertTrue(rs.getResultMessage().contains(DASHBOARD));
    }

    private Dashboard getNotOwnerDashboard() {
        Acl acl = new Acl();
        acl.setOwnerUserId("not_owner");
        AclEntry entry = new AclEntry();
        entry.setProjectId(PROJECT);
        acl.addEntry(entry);
        Dashboard dashboard = new Dashboard();
        dashboard.setName(DASHBOARD);
        dashboard.setAcl(acl);
        dashboard.setProjectName(PROJECT);
        return dashboard;
    }

    public Dashboard getOwnerDashboard() {
        Acl acl = new Acl();
        acl.setOwnerUserId(OWNER);
        Dashboard dashboard = new Dashboard();
        dashboard.setName(DASHBOARD);
        dashboard.setAcl(acl);
        dashboard.setProjectName(PROJECT);
        return dashboard;
    }

    public Map<String, ProjectRole> getProjectRoles() {
        return ImmutableMap.<String, ProjectRole>builder().put(PROJECT, ProjectRole.MEMBER).build();
    }

    public Map<String, ProjectRole> getProjectRolesForPM() {
        return ImmutableMap.<String, ProjectRole>builder().put(PROJECT, ProjectRole.PROJECT_MANAGER).build();
    }
}