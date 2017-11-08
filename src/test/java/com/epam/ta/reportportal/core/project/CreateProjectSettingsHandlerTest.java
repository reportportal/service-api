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
import com.epam.ta.reportportal.core.project.settings.impl.CreateProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateProjectSettingsHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CreateProjectSettingsHandler projectSettingsHandler;
	private String project = "project";

	@Before
	public void before() {
		ProjectRepository projectRepository = mock(ProjectRepository.class);
		when(projectRepository.findOne(project)).thenReturn(new Project());

		projectSettingsHandler = new CreateProjectSettingsHandler(projectRepository, mock(WidgetRepository.class),
				mock(ApplicationEventPublisher.class)
		);

	}

	@Test
	public void projectNotExists() {
		final String notExists = "notExists";
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(PROJECT_NOT_FOUND.getDescription(), notExists).get());
		projectSettingsHandler.createProjectIssueSubType(notExists, AuthConstants.TEST_USER, new CreateIssueSubTypeRQ());
	}

	@Test
	public void toInvestigateSubtype() {
		CreateIssueSubTypeRQ rq = new CreateIssueSubTypeRQ();
		rq.setTypeRef("tO_inVestigate");
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Impossible to create sub-type for 'To Investigate' type.'");
		projectSettingsHandler.createProjectIssueSubType(project, AuthConstants.TEST_USER, rq);
	}

	@Test
	public void notIssueSubtype() {
		CreateIssueSubTypeRQ rq = new CreateIssueSubTypeRQ();
		rq.setTypeRef("nOt_isSue");
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Impossible to create sub-type for 'Not Issue' type.'");
		projectSettingsHandler.createProjectIssueSubType(project, AuthConstants.TEST_USER, rq);
	}

}