/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.core.project.settings.impl.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.ISSUE_TYPE_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteProjectSettingsHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static DeleteProjectSettingsHandler deleteProjectSettingsHandler;
	private String notExists = "notExists";
	private static String project = "project";
	private static String withoutSettings = "withoutSettings";
	private static String subType = "subType";

	@BeforeClass
	public static void setUp() {
		deleteProjectSettingsHandler = new DeleteProjectSettingsHandler();
		ProjectRepository projectRepository = mock(ProjectRepository.class);
		when(projectRepository.findOne(withoutSettings)).thenReturn(new Project());
		when(projectRepository.findOne(project)).thenReturn(new Project());
		deleteProjectSettingsHandler.setProjectRepository(projectRepository);

	}

	@Test
	public void projectNotExists() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(PROJECT_NOT_FOUND.getDescription(), notExists).get());
		deleteProjectSettingsHandler.deleteProjectIssueSubType(notExists, "", AuthConstants.TEST_USER);
	}

	//	@Test
	//	public void settingsNotExists() {
	//		thrown.expect(ReportPortalException.class);
	//		thrown.expectMessage(formattedSupplier(PROJECT_SETTINGS_NOT_FOUND.getDescription(), withoutSettings).get());
	//		deleteProjectSettingsHandler.deleteProjectIssueSubType(withoutSettings, "", AuthConstants.TEST_USER);
	//	}

	@Test
	public void subTypeNotExists() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(ISSUE_TYPE_NOT_FOUND.getDescription(), subType).get());
		deleteProjectSettingsHandler.deleteProjectIssueSubType(project, AuthConstants.TEST_USER, subType);
	}

	@Test
	public void deletePredefinedSubtype() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. You cannot remove predefined global issue types");
		deleteProjectSettingsHandler.deleteProjectIssueSubType(project, AuthConstants.TEST_USER, "no_defect");
	}

}