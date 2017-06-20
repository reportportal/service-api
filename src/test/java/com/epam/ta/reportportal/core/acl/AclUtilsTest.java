package com.epam.ta.reportportal.core.acl;

import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public class AclUtilsTest {

    private static final String OWNER_ID = "Owner";
    private static final String NOT_OWNER = "Not_owner";

    @Test
    public void deleteByOwner() {
        Assert.assertTrue(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(),
                OWNER_ID, ProjectRole.MEMBER, UserRole.USER));
    }

    @Test
    public void deleteNotByOwner() {
        Assert.assertFalse(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(),
                NOT_OWNER, ProjectRole.MEMBER, UserRole.USER));
    }

    @Test
    public void deleteByAdminFromHisDashboard() {
        Assert.assertFalse(AclUtils.isAllowedToDeleteWidget(modifierDashboard(), widgetAcl(), NOT_OWNER,
                ProjectRole.MEMBER, UserRole.ADMINISTRATOR));
    }

    @Test
    public void deleteByAdminFromSharedDashboard() {
        Assert.assertTrue(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), NOT_OWNER,
                ProjectRole.MEMBER, UserRole.ADMINISTRATOR));
    }

    @Test
    public void deleteByPMFromHisDashboard() {
        Assert.assertFalse(AclUtils.isAllowedToDeleteWidget(modifierDashboard(), widgetAcl(), NOT_OWNER,
                ProjectRole.PROJECT_MANAGER, UserRole.USER));
    }

    @Test
    public void deleteByPMFromSharedDashboard() {
        Assert.assertTrue(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), NOT_OWNER,
                ProjectRole.PROJECT_MANAGER, UserRole.USER));
    }

    private Acl modifierDashboard() {
        Acl acl = new Acl();
        acl.setOwnerUserId(NOT_OWNER);
        acl.setEntries(ImmutableSet.<AclEntry>builder().add(new AclEntry()).build());
        return acl;
    }

    private Acl dashboardAcl() {
        Acl acl = new Acl();
        acl.setOwnerUserId(OWNER_ID);
        acl.setEntries(ImmutableSet.<AclEntry>builder().add(new AclEntry()).build());
        return acl;
    }

    private Acl widgetAcl() {
        Acl acl = new Acl();
        acl.setOwnerUserId(OWNER_ID);
        acl.setEntries(ImmutableSet.<AclEntry>builder().add(new AclEntry()).build());
        return acl;
    }

}