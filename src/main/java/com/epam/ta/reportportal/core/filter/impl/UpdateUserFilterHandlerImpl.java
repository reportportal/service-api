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
import com.epam.ta.reportportal.auth.acl.ReportPortalAclHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterUpdatedEvent;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.USER_FILTER_NOT_FOUND;

@Service
public class UpdateUserFilterHandlerImpl implements IUpdateUserFilterHandler {

	private final UserFilterRepository userFilterRepository;

	private final ReportPortalAclHandler aclHandler;

	private final MessageBus messageBus;

	@Autowired
	public UpdateUserFilterHandlerImpl(UserFilterRepository userFilterRepository, ReportPortalAclHandler aclHandler,
			MessageBus messageBus) {
		this.userFilterRepository = userFilterRepository;
		this.aclHandler = aclHandler;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateUserFilter(Long userFilterId, UpdateUserFilterRQ updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter userFilter = userFilterRepository.findById(userFilterId)
				.orElseThrow(() -> new ReportPortalException(USER_FILTER_NOT_FOUND,
						userFilterId,
						projectDetails.getProjectId(),
						user.getUserId()
				));
		expect(userFilter.getProject().getId(), Predicate.isEqual(projectDetails.getProjectId())).verify(USER_FILTER_NOT_FOUND,
				userFilterId,
				projectDetails.getProjectId(),
				user.getUserId()
		);
		UserFilterActivityResource before = TO_ACTIVITY_RESOURCE.apply(userFilter);
		UserFilter updated = new UserFilterBuilder(userFilter).addUpdateFilterRQ(updateRQ).get();

		if (before.isShared() != updated.isShared()) {
			aclHandler.updateAcl(updated, projectDetails.getProjectId(), updated.isShared());
		}

		messageBus.publishActivity(new FilterUpdatedEvent(before, TO_ACTIVITY_RESOURCE.apply(updated), user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + updated.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
