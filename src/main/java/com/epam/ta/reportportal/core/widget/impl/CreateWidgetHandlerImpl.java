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

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateWidgetHandlerImpl implements ICreateWidgetHandler {

	private WidgetRepository widgetRepository;

	private UserFilterRepository filterRepository;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setUserFilterRepository(UserFilterRepository filterRepository) {
		this.filterRepository = filterRepository;
	}

	@Override
	public EntryCreatedRS createWidget(WidgetRQ createWidgetRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		UserFilter userFilter = filterRepository.findById(createWidgetRQ.getFilterId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, createWidgetRQ.getFilterId()));

		Widget widget = new WidgetBuilder().addWidgetRq(createWidgetRQ)
				.addProject(projectDetails.getProjectId())
				.addFilter(userFilter)
				.get();
		widgetRepository.save(widget);

		return new EntryCreatedRS(widget.getId());
	}
}
