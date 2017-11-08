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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.core.admin.ServerAdminHandler;
import com.epam.ta.reportportal.ws.controller.ISettingsController;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * Basic implementation of administrator interface {@link ISettingsController}
 *
 * @author Andrei_Ramanchuk
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Controller
@RequestMapping("/settings")
@PreAuthorize(ADMIN_ONLY)
public class SettingsController implements ISettingsController {

	@Autowired
	private ServerAdminHandler serverHandler;

	@Override
	@RequestMapping(value = "/{profileId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Get server email settings for specified profile", notes = "'default' profile is using till additional UI implementations")
	public ServerSettingsResource getServerSettings(@PathVariable String profileId, Principal principal) {
		return serverHandler.getServerSettings(normalizeId(profileId));
	}

	@Override
	@RequestMapping(value = "/{profileId}/email", method = { RequestMethod.POST, RequestMethod.PUT })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Set server email settings for specified profile", notes = "'default' profile is using till additional UI implementations")
	public OperationCompletionRS saveEmailSettings(@PathVariable String profileId, @RequestBody @Validated ServerEmailResource request,
			Principal principal) {
		return serverHandler.saveEmailSettings(normalizeId(profileId), request);
	}

	@RequestMapping(value = "/{profileId}/email", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Deletes email settings for specified profile", notes = "'default' profile is using till additional UI implementations")
	public OperationCompletionRS deleteEmailSettings(@PathVariable String profileId) {
		return serverHandler.deleteEmailSettings(profileId);
	}

	@RequestMapping(value = "/{profileId}/analytics", method = { RequestMethod.PUT, RequestMethod.POST })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Updates analytics settings for specified profile")
	public OperationCompletionRS saveAnalyticsSettings(@PathVariable String profileId, @RequestBody @Validated AnalyticsResource request) {
		return serverHandler.saveAnalyticsSettings(normalizeId(profileId), request);
	}
}