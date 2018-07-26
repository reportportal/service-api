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
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateWidgetHandlerImpl implements IUpdateWidgetHandler {

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

	@Override
	public OperationCompletionRS updateWidget(Long widgetId, WidgetRQ updateRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Widget widget = widgetRepository.findById(widgetId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND, widgetId));
		UserFilter userFilter = filterRepository.findById(updateRQ.getFilterId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, updateRQ.getFilterId()));
		widget = new WidgetBuilder(widget).addWidgetRq(updateRQ).addFilter(userFilter).get();
		widgetRepository.save(widget);
		return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' successfully updated.");
	}
}
