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

import com.epam.ta.reportportal.core.project.settings.impl.GetProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.ProjectSettingsResourceAssembler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetProjectSettingsHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void projectNotExists() {
		String notExists = "notExists";
		ProjectRepository settingsRepository = mock(ProjectRepository.class);
		when(settingsRepository.findOne(notExists)).thenReturn(null);
		GetProjectSettingsHandler settingsHandler = new GetProjectSettingsHandler(settingsRepository,
				mock(ProjectSettingsResourceAssembler.class)
		);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(PROJECT_NOT_FOUND.getDescription(), notExists).get());
		settingsHandler.getProjectSettings(notExists);
	}
}