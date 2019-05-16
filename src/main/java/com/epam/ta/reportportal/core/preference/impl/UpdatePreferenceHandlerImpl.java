/*
 * Copyright (C) 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.preference.UserPreference;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.UserPreferenceBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
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
	private final GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@Autowired
	public UpdatePreferenceHandlerImpl(UserPreferenceRepository userPreferenceRepository,
			GetShareableEntityHandler<UserFilter> getShareableEntityHandler) {
		this.userPreferenceRepository = userPreferenceRepository;
		this.getShareableEntityHandler = getShareableEntityHandler;
	}

	@Override
	public OperationCompletionRS addPreference(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long filterId) {
		UserFilter filter = getShareableEntityHandler.getPermitted(filterId, projectDetails);
		UserPreference userPreference = new UserPreferenceBuilder().withUser(user.getUserId())
				.withProject(projectDetails.getProjectId())
				.withFilter(filter)
				.get();
		userPreferenceRepository.save(userPreference);
		return new OperationCompletionRS("Filter with id = " + filterId + " successfully added to launches tab.");
	}

	@Override
	public OperationCompletionRS removePreference(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long filterId) {
		UserPreference userPreference = userPreferenceRepository.findByProjectIdAndUserId(projectDetails.getProjectId(), user.getUserId())
				.stream()
				.filter(it -> it.getFilter().getId().equals(filterId))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, filterId));
		userPreferenceRepository.delete(userPreference);
		return new OperationCompletionRS("Filter with id = " + filterId + " successfully removed from launches tab.");
	}
}