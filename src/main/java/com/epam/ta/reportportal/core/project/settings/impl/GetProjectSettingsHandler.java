/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.project.settings.impl;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.project.settings.IGetProjectSettingsHandler;
import com.epam.ta.reportportal.database.dao.ProjectSettingsRepository;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.ws.converter.ProjectSettingsResourceAssembler;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;

/**
 * Initial implementation of
 * {@link com.epam.ta.reportportal.core.project.settings.IGetProjectSettingsHandler}
 * 
 * @author Andrei_Ramanchuk
 *
 */
@Service
public class GetProjectSettingsHandler implements IGetProjectSettingsHandler {

	private ProjectSettingsRepository settingsRepo;

	private ProjectSettingsResourceAssembler assembler;

	@Autowired
	public GetProjectSettingsHandler(ProjectSettingsRepository projectSettingsRepository,
			ProjectSettingsResourceAssembler projectSettingsResourceAssembler) {
		this.settingsRepo = projectSettingsRepository;
		this.assembler = projectSettingsResourceAssembler;
	}

	@Override
	public ProjectSettingsResource getProjectSettings(String projectName) {
		ProjectSettings settings = settingsRepo.findOne(projectName);
		expect(settings, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		return assembler.toResource(settings);
	}
}