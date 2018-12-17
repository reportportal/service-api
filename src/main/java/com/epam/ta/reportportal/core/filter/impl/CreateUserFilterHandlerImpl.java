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
import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterCreatedEvent;
import com.epam.ta.reportportal.core.filter.ICreateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateUserFilterHandlerImpl implements ICreateUserFilterHandler {

	private final ShareableObjectsHandler aclHandler;

	private final UserFilterRepository userFilterRepository;

	private final MessageBus messageBus;

	@Autowired
	public CreateUserFilterHandlerImpl(ShareableObjectsHandler aclHandler, UserFilterRepository userFilterRepository,
			MessageBus messageBus) {
		this.aclHandler = aclHandler;
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
	}

	@Override
	public EntryCreatedRS createFilter(CreateUserFilterRQ createFilterRQ, String projectName, ReportPortalUser user) {

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, projectName);

		BusinessRule.expect(userFilterRepository.existsByNameAndOwnerAndProjectId(
				createFilterRQ.getName(),
				user.getUsername(),
				projectDetails.getProjectId()
		), BooleanUtils::isFalse).verify(ErrorType.USER_FILTER_ALREADY_EXISTS, createFilterRQ.getName(), user.getUsername(), projectName);

		UserFilter filter = new UserFilterBuilder().addCreateRq(createFilterRQ)
				.addProject(projectDetails.getProjectId())
				.addOwner(user.getUsername())
				.get();
		userFilterRepository.save(filter);
		aclHandler.initAcl(filter, user.getUsername(), projectDetails.getProjectId(), BooleanUtils.isTrue(createFilterRQ.getShare()));
		messageBus.publishActivity(new FilterCreatedEvent(TO_ACTIVITY_RESOURCE.apply(filter), user.getUserId()));
		return new EntryCreatedRS(filter.getId());
	}
}
