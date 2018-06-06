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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/widget")
public class WidgetController {

	private ICreateWidgetHandler createWidgetHandler;

	@Autowired
	public void setCreateWidgetHandler(ICreateWidgetHandler createWidgetHandler) {
		this.createWidgetHandler = createWidgetHandler;
	}

	@Transactional
	@PostMapping
	@ResponseBody
	@ResponseStatus(CREATED)
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS createWidget(@RequestBody WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {
		return createWidgetHandler.createWidget(createWidget, EntityUtils.takeProjectDetails(user, projectName), user);
	}

	@GetMapping("/{widgetId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	public String getWidgetById(@PathVariable Long widgetId, @AuthenticationPrincipal ReportPortalUser user) {
		return "ok";
	}

}
