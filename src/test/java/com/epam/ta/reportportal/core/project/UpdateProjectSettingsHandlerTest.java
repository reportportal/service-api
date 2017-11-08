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

import com.epam.ta.reportportal.core.project.settings.impl.UpdateProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.UpdateOneIssueSubTypeRQ;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateProjectSettingsHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private UpdateProjectSettingsHandler updateProjectSettingsHandler;
	private String project = "project";
	private String user = "user";

	@Before
	public void before() {

		ProjectRepository projectRepo = mock(ProjectRepository.class);
		when(projectRepo.findOne(project)).thenReturn(new Project());
		updateProjectSettingsHandler = new UpdateProjectSettingsHandler(projectRepo, mock(ApplicationEventPublisher.class));

	}

	@Test
	public void updateNotIssueSubType() {

		final UpdateIssueSubTypeRQ rq = new UpdateIssueSubTypeRQ();
		final UpdateOneIssueSubTypeRQ subTypeRQ = new UpdateOneIssueSubTypeRQ();
		final String notIssue = "nOt_issUe";
		subTypeRQ.setTypeRef(notIssue);
		subTypeRQ.setId(notIssue);
		rq.setIds(Collections.singletonList(subTypeRQ));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Issue Type '" + notIssue + "' not found.");
		updateProjectSettingsHandler.updateProjectIssueSubType(project, user, rq);
	}

	@Test
	public void updatePredefinedSubType() {
		final UpdateIssueSubTypeRQ rq = new UpdateIssueSubTypeRQ();
		final UpdateOneIssueSubTypeRQ subTypeRQ = new UpdateOneIssueSubTypeRQ();
		subTypeRQ.setTypeRef("product_bug");
		subTypeRQ.setId("pb001");
		rq.setIds(Collections.singletonList(subTypeRQ));
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. You cannot edit predefined global issue types.");
		updateProjectSettingsHandler.updateProjectIssueSubType(project, user, rq);
	}

}