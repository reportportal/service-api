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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.project.settings.impl.CreateProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.impl.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.impl.GetProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.impl.UpdateProjectSettingsHandler;
import com.epam.ta.reportportal.ws.controller.IProjectSettingsController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Projects settings controller implementation of
 * {@link com.epam.ta.reportportal.ws.controller.IProjectSettingsController}<br>
 * Provides resources for manipulation of various project settings items.
 *
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/settings")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ProjectSettingsController implements IProjectSettingsController {

	@Autowired
	private CreateProjectSettingsHandler createSettings;

	@Autowired
	private UpdateProjectSettingsHandler updateSettings;

	@Autowired
	private DeleteProjectSettingsHandler deleteSettings;

	@Autowired
	private GetProjectSettingsHandler getSettings;

	@Override
	@RequestMapping(value = "/sub-type", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Creation of custom project specific issue sub-type")
	public EntryCreatedRS createProjectIssueSubType(@PathVariable String projectName, @RequestBody @Validated CreateIssueSubTypeRQ request,
			Principal principal) {
		return createSettings.createProjectIssueSubType(EntityUtils.normalizeProjectName(projectName), principal.getName(), request);
	}

	@Override
	@RequestMapping(value = "/sub-type", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update of custom project specific issue sub-type")
	public OperationCompletionRS updateProjectIssueSubType(@PathVariable String projectName,
			@RequestBody @Validated UpdateIssueSubTypeRQ request, Principal principal) {
		return updateSettings.updateProjectIssueSubType(EntityUtils.normalizeId(projectName), principal.getName(), request);
	}

	@Override
	@RequestMapping(value = "/sub-type/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Delete custom project specific issue sub-type")
	public OperationCompletionRS deleteProjectIssueSubType(@PathVariable String projectName, @PathVariable String id, Principal principal) {
		return deleteSettings.deleteProjectIssueSubType(EntityUtils.normalizeId(projectName), principal.getName(), id);
	}

	@Override
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get project specific issue sub-types", notes = "Only for users that are assigned to the project")
	public ProjectSettingsResource getProjectSettings(@PathVariable String projectName, Principal principal) {
		return getSettings.getProjectSettings(EntityUtils.normalizeId(projectName));
	}
}
