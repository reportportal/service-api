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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.WidgetUpdatedEvent;
import com.epam.ta.reportportal.core.widget.IShareWidgetHandler;
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateWidgetHandlerImpl implements IUpdateWidgetHandler {

	private WidgetRepository widgetRepository;

	private UserFilterRepository filterRepository;

	private MessageBus messageBus;

	@Autowired
	private IShareWidgetHandler shareWidgetHandler;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setFilterRepository(UserFilterRepository filterRepository) {
		this.filterRepository = filterRepository;
	}

	@Autowired
	public void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateWidget(Long widgetId, WidgetRQ updateRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Widget widget = shareWidgetHandler.findById(widgetId);
		Widget before = SerializationUtils.clone(widget);
		List<UserFilter> userFilter = null;
		List<Long> filterIds = updateRQ.getFilterIds();

		//TODO check if this statement could be replaced by loop filter search
		if (CollectionUtils.isNotEmpty(filterIds)) {
			userFilter = filterRepository.findAllById(filterIds);
//					.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, updateRQ.getFilterId()));
		}
		widget = new WidgetBuilder(widget).addWidgetRq(updateRQ).addFilters(userFilter).get();
		widgetRepository.save(widget);
		messageBus.publishActivity(new WidgetUpdatedEvent(before, widget, user.getUserId()));
		return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' successfully updated.");
	}
}
