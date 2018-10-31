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

/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.preference.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler;
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
 * @author Dzmitry_Kavalets
 */
@Service
public class UpdatePreferenceHandlerImpl implements UpdatePreferenceHandler {

	private final UserPreferenceRepository userPreferenceRepository;

	private final GetUserFilterHandler getUserFilterHandler;

	@Autowired
	public UpdatePreferenceHandlerImpl(UserPreferenceRepository userPreferenceRepository, GetUserFilterHandler getUserFilterHandler) {
		this.userPreferenceRepository = userPreferenceRepository;
		this.getUserFilterHandler = getUserFilterHandler;
	}

	@Override
	public OperationCompletionRS addPreference(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long filterId) {
		UserFilter filter = getUserFilterHandler.getFilter(filterId, projectDetails, user);
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