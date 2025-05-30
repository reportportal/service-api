/*
 * Copyright 2019 EPAM Systems
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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.reportportal.rules.exception.ErrorType.USER_FILTER_NOT_FOUND;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterDeletedEvent;
import com.epam.ta.reportportal.core.filter.DeleteUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserFilterHandlerImpl implements DeleteUserFilterHandler {

  private final UserFilterRepository userFilterRepository;
  private final MessageBus messageBus;

	@Autowired
	public DeleteUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus) {
    this.userFilterRepository = userFilterRepository;
    this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS deleteFilter(Long id, MembershipDetails membershipDetails, ReportPortalUser user) {
		UserFilter userFilter = userFilterRepository.findByIdAndProjectId(id, membershipDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT,
						id,
						membershipDetails.getProjectName()
				));
		expect(userFilter.getProject().getId(), Predicate.isEqual(membershipDetails.getProjectId())).verify(USER_FILTER_NOT_FOUND,
				id,
				membershipDetails.getProjectId(),
				user.getUserId()
		);
		userFilterRepository.delete(userFilter);
		messageBus.publishActivity(new FilterDeletedEvent(TO_ACTIVITY_RESOURCE.apply(userFilter), user.getUserId(), user.getUsername()));
		return new OperationCompletionRS("User filter with ID = '" + id + "' successfully deleted.");
	}
}
