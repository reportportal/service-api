/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterDeletedEvent;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;

@Service
public class DeleteUserFilterHandlerImpl implements IDeleteUserFilterHandler {

	private final UserFilterRepository userFilterRepository;
	private final MessageBus messageBus;
	private final GetUserFilterHandler getFilterHandler;

	@Autowired
	public DeleteUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus,
			GetUserFilterHandler getFilterHandler) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
		this.getFilterHandler = getFilterHandler;
	}

	@Override
	public OperationCompletionRS deleteFilter(Long id, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter userFilter = getFilterHandler.getFilter(id, projectDetails, user);
		userFilterRepository.delete(userFilter);
		messageBus.publishActivity(new FilterDeletedEvent(TO_ACTIVITY_RESOURCE.apply(userFilter), user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + id + "' successfully deleted.");
	}
}
