/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

		List<UserFilter> userFilter = null;
		//TODO chech if this statement could be replaced by loop filter search
		if (CollectionUtils.isNotEmpty(createWidgetRQ.getFilterIds())) {
			userFilter = filterRepository.findAllById(createWidgetRQ.getFilterIds());
//					.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, createWidgetRQ.getFilterId()));
		}

		Widget widget = new WidgetBuilder().addWidgetRq(createWidgetRQ)
				.addProject(projectDetails.getProjectId())
				.addFilters(userFilter)
				.get();
		widgetRepository.save(widget);

		return new EntryCreatedRS(widget.getId());
	}
}
