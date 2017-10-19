/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.acl;

import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel_Bortnik
 */
public class AclUtilsTest {

	private static final String OWNER_ID = "Owner";
	private static final String NOT_OWNER = "Not_owner";

	@Test
	public void deleteByOwner() {
		Assert.assertTrue(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), OWNER_ID, ProjectRole.MEMBER, UserRole.USER));
	}

	@Test
	public void deleteNotByOwner() {
		Assert.assertFalse(AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), NOT_OWNER, ProjectRole.MEMBER, UserRole.USER));
	}

	@Test
	public void deleteByAdminFromHisDashboard() {
		Assert.assertFalse(
				AclUtils.isAllowedToDeleteWidget(modifierDashboard(), widgetAcl(), NOT_OWNER, ProjectRole.MEMBER, UserRole.ADMINISTRATOR));
	}

	@Test
	public void deleteByAdminFromSharedDashboard() {
		Assert.assertTrue(
				AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), NOT_OWNER, ProjectRole.MEMBER, UserRole.ADMINISTRATOR));
	}

	@Test
	public void deleteByPMFromHisDashboard() {
		Assert.assertFalse(
				AclUtils.isAllowedToDeleteWidget(modifierDashboard(), widgetAcl(), NOT_OWNER, ProjectRole.PROJECT_MANAGER, UserRole.USER));
	}

	@Test
	public void deleteByPMFromSharedDashboard() {
		Assert.assertTrue(
				AclUtils.isAllowedToDeleteWidget(dashboardAcl(), widgetAcl(), NOT_OWNER, ProjectRole.PROJECT_MANAGER, UserRole.USER));
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