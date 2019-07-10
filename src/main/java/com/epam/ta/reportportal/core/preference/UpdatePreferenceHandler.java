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

package com.epam.ta.reportportal.core.preference;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Update user preference handler
 *
 * @author Pavel Bortnik
 */
public interface UpdatePreferenceHandler {
	/**
	 * Add user preference
	 *
	 * @param projectDetails Project Details
	 * @param user           User
	 * @param filterId       Adding filter id
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS addPreference(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long filterId);

	/**
	 * Remove user preference
	 *
	 * @param projectDetails Project Details
	 * @param user           User
	 * @param filterId       Removing filter id
	 * @return {@link OperationCompletionRS}
	 */
	OperationCompletionRS removePreference(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Long filterId);
}