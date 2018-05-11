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
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.WidgetRepository;
import com.epam.ta.reportportal.store.database.entity.JsonbObject;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.store.database.entity.widget.WidgetOption;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/widget")
public class WidgetController {

	@Autowired
	private WidgetRepository widgetRepository;

	@PostMapping
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Start a root test item")
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS startRootItem(@RequestBody WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {

		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		Project project = new Project();
		project.setId(projectDetails.getProjectId());

		WidgetOption widgetOption = new WidgetOption();
		widgetOption.setOptions(createWidget.getContentParameters().getWidgetOptions());

		Widget widget = new Widget();
		widget.setName(createWidget.getName());
		widget.setProject(project);
		widget.setWidgetOptions(widgetOption);
		widget.setWidgetType(createWidget.getContentParameters().getType());

		Widget save = widgetRepository.save(widget);

		return new EntryCreatedRS(save.getId());

	}

	@GetMapping("/{widgetId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	public String getWidgetById(@PathVariable Long widgetId, @AuthenticationPrincipal ReportPortalUser user) {
		Widget one = widgetRepository.getOne(widgetId);
		return one.getWidgetOptions().getOptions().get("filterName").get(0);
	}

	public static void main(String[] args) throws IOException {
		WidgetOption widgetOption = new WidgetOption();
		Map<String, List<String>> map = new HashMap<>();
		map.put("filterName", Lists.newArrayList("objectFilter"));
		widgetOption.setOptions(map);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
		String s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(widgetOption);
		WidgetOption jsonbObject = (WidgetOption) objectMapper.readValue(s, JsonbObject.class);
		System.out.println(jsonbObject.getOptions().get("filterName"));
	}

}
