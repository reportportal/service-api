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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.acl.ReportPortalAclService;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterCreatedEvent;
import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateUserFilterHandlerImpl implements ICreateUserFilterHandler {


	@Autowired
	private ReportPortalAclService aclService;

	private final UserFilterRepository userFilterRepository;

	private final MessageBus messageBus;

	@Autowired
	public CreateUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
	}

	@Override
	public EntryCreatedRS createFilter(CreateUserFilterRQ createFilterRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		UserFilter filter = new UserFilterBuilder().addCreateRq(createFilterRQ).addProject(projectDetails.getProjectId()).get();
		userFilterRepository.save(filter);
        aclService.createAcl(filter);
        aclService.addReadPermissions(filter, user.getUsername());
		messageBus.publishActivity(new FilterCreatedEvent(filter, user.getUserId()));
		return new EntryCreatedRS(filter.getId());
	}
}
