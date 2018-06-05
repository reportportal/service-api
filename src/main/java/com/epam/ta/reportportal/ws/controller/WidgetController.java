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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.store.database.dao.WidgetRepository;
import com.epam.ta.reportportal.store.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.store.database.entity.widget.WidgetOption;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/widget")
public class WidgetController {

	private WidgetRepository widgetRepository;

	private UserFilterRepository filterRepository;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setFilterRepository(UserFilterRepository filterRepository) {
		this.filterRepository = filterRepository;
	}

	@PostMapping
	@Transactional
	@ResponseBody
	@ResponseStatus(CREATED)
	//@PreAuthorize(ALLOWED_TO_REPORT)
	public EntryCreatedRS createWidget(@RequestBody WidgetRQ createWidget, @AuthenticationPrincipal ReportPortalUser user,
			@PathVariable String projectName) {

		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		Project project = new Project();
		project.setId(projectDetails.getProjectId());

		UserFilter userFilter = filterRepository.findById(createWidget.getFilterId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND));

		Set<WidgetOption> widgetOptions = createWidget.getContentParameters().getWidgetOptions().entrySet().stream().map(entry -> {
			WidgetOption option = new WidgetOption();
			option.setWidgetOption(entry.getKey());
			option.setValues(Sets.newHashSet(entry.getValue()));
			return option;
		}).collect(toSet());

		Widget widget = new Widget();
		widget.setName(createWidget.getName());
		widget.setProject(project);
		widget.setWidgetOptions(widgetOptions);
		widget.setWidgetType(createWidget.getContentParameters().getType());
		widget.setItemsCount(createWidget.getContentParameters().getItemsCount());
		widget.setContentFields(createWidget.getContentParameters().getContentFields());

		widget.getFilters().add(userFilter);
		userFilter.getWidgets().add(widget);

		Widget save = widgetRepository.save(widget);

		return new EntryCreatedRS(save.getId());

	}

	@GetMapping("/{widgetId}")
	@Transactional
	@ResponseBody
	@ResponseStatus(OK)
	public String getWidgetById(@PathVariable Long widgetId, @AuthenticationPrincipal ReportPortalUser user) {
		Widget one = widgetRepository.getOne(widgetId);
		return "ok";
	}

}
