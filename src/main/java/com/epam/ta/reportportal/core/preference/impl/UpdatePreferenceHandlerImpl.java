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

package com.epam.ta.reportportal.core.preference.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.preference.UserPreference;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.UserPreferenceBuilder;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of
 * {@link com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler}
 *
 * @author Pavel Bortnik
 */
@Service
public class UpdatePreferenceHandlerImpl implements UpdatePreferenceHandler {

	private final UserPreferenceRepository userPreferenceRepository;
	private final UserFilterRepository userFilterRepository;

	@Autowired
	public UpdatePreferenceHandlerImpl(UserPreferenceRepository userPreferenceRepository, UserFilterRepository userFilterRepository) {
		this.userPreferenceRepository = userPreferenceRepository;
		this.userFilterRepository = userFilterRepository;
	}

	@Override
	public OperationCompletionRS addPreference(MembershipDetails membershipDetails, ReportPortalUser user, Long filterId) {

		if (userPreferenceRepository.findByProjectIdAndUserIdAndFilterId(membershipDetails.getProjectId(), user.getUserId(), filterId)
				.isPresent()) {
			throw new ReportPortalException(ErrorType.RESOURCE_ALREADY_EXISTS, "User Preference");
		}

		UserFilter filter = userFilterRepository.findByIdAndProjectId(filterId, membershipDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT,
						filterId,
            membershipDetails.getProjectName()
				));

		UserPreference userPreference = new UserPreferenceBuilder().withUser(user.getUserId())
				.withProject(membershipDetails.getProjectId())
				.withFilter(filter)
				.get();
		userPreferenceRepository.save(userPreference);
		return new OperationCompletionRS("Filter with id = " + filterId + " successfully added to launches tab.");
	}

	@Override
	public OperationCompletionRS removePreference(MembershipDetails membershipDetails, ReportPortalUser user, Long filterId) {
		UserPreference userPreference = userPreferenceRepository.findByProjectIdAndUserIdAndFilterId(membershipDetails.getProjectId(),
						user.getUserId(),
						filterId
				)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, filterId));
		userPreferenceRepository.delete(userPreference);
		return new OperationCompletionRS("Filter with id = " + filterId + " successfully removed from launches tab.");
	}
}
