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
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.WidgetRepository;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetWidgetHandlerImpl implements IGetWidgetHandler {

	private WidgetRepository widgetRepository;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Override
	public WidgetResource getWidget(Long widgetId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Widget widget = widgetRepository.findById(widgetId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND, widgetId));
		return WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);
	}

	@Override
	public Iterable<SharedEntity> getSharedWidgetNames(String userName, String projectName, Pageable pageable) {
		return null;
	}

	@Override
	public Iterable<WidgetResource> getSharedWidgetsList(String userName, String projectName, Pageable pageable) {
		return null;
	}

	@Override
	public List<String> getWidgetNames(String projectName, String userName) {
		return null;
	}

	@Override
	public Map<String, ?> getWidgetPreview(String projectName, String userName, WidgetPreviewRQ previewRQ) {
		return null;
	}

	@Override
	public Iterable<WidgetResource> searchSharedWidgets(String term, String projectName, Pageable pageable) {
		return null;
	}
}
