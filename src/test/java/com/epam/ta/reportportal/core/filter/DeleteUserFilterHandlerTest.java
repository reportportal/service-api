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
package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.core.filter.impl.DeleteUserFilterHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * @author Pavel Bortnik
 */
public class DeleteUserFilterHandlerTest {

	private static final String SIMPLE_USER = "simple";
	private static final String FILTER = "filter";
	private static final String PROJECT = "project";

	private final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final static ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

	private DeleteUserFilterHandler handler = new DeleteUserFilterHandler(userFilterRepository, projectRepository, eventPublisher);

	@BeforeClass
	public static void beforeClass() {
		doNothing().when(eventPublisher).publishEvent(anyObject());
	}

	@Test
	public void deleteFilterByOwner() {
		when(userFilterRepository.findOne(FILTER)).thenReturn(getOwnerFilter());
		when(projectRepository.findProjectRoles(SIMPLE_USER)).thenReturn(getProjectRolesForSimpleUser());
		OperationCompletionRS operationCompletionRS = handler.deleteFilter(FILTER, SIMPLE_USER, PROJECT, UserRole.USER);
		assertTrue(operationCompletionRS.getResultMessage().contains(FILTER));
	}

	@Test(expected = ReportPortalException.class)
	public void negativeDeleteFilter() {
		when(userFilterRepository.findOne(FILTER)).thenReturn(getNotOwnerFilter());
		when(projectRepository.findProjectRoles(SIMPLE_USER)).thenReturn(getProjectRolesForSimpleUser());
		handler.deleteFilter(FILTER, SIMPLE_USER, PROJECT, UserRole.USER);
	}

	@Test(expected = ReportPortalException.class)
	public void negativeDeleteFilterNull() {
		when(userFilterRepository.findOne(FILTER)).thenReturn(null);
		handler.deleteFilter(FILTER, SIMPLE_USER, PROJECT, UserRole.USER);
	}

	@Test
	public void deleteFilterByPm() {
		when(userFilterRepository.findOne(FILTER)).thenReturn(getNotOwnerFilter());
		when(projectRepository.findProjectRoles(SIMPLE_USER)).thenReturn(getProjectRolesForSimpleUserWithPmRole());
		OperationCompletionRS operationCompletionRS = handler.deleteFilter(FILTER, SIMPLE_USER, PROJECT, UserRole.USER);
		assertTrue(operationCompletionRS.getResultMessage().contains(FILTER));
	}

	@Test
	public void deleteFilterByAdmin() {
		when(userFilterRepository.findOne(FILTER)).thenReturn(getNotOwnerFilter());
		when(projectRepository.findProjectRoles(SIMPLE_USER)).thenReturn(getProjectRolesForSimpleUser());
		OperationCompletionRS rs = handler.deleteFilter(FILTER, SIMPLE_USER, PROJECT, UserRole.ADMINISTRATOR);
		assertTrue(rs.getResultMessage().contains(FILTER));
	}

	private UserFilter getNotOwnerFilter() {
		Acl acl = new Acl();
		acl.setOwnerUserId("not_owner");
		AclEntry entry = new AclEntry();
		entry.setProjectId(PROJECT);
		acl.addEntry(entry);
		UserFilter filter = new UserFilter();
		filter.setName(FILTER);
		filter.setAcl(acl);
		filter.setProjectName(PROJECT);
		return filter;
	}

	private UserFilter getOwnerFilter() {
		Acl acl = new Acl();
		acl.setOwnerUserId(SIMPLE_USER);
		UserFilter filter = new UserFilter();
		filter.setName(FILTER);
		filter.setAcl(acl);
		filter.setProjectName(PROJECT);
		return filter;
	}

	public Map<String, ProjectRole> getProjectRolesForSimpleUser() {
		return ImmutableMap.<String, ProjectRole>builder().put(PROJECT, ProjectRole.MEMBER).build();
	}

	public Map<String, ProjectRole> getProjectRolesForSimpleUserWithPmRole() {
		return ImmutableMap.<String, ProjectRole>builder().put(PROJECT, ProjectRole.PROJECT_MANAGER).build();
	}
}