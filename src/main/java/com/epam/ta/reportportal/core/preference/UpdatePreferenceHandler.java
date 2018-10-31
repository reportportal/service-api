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

package com.epam.ta.reportportal.core.preference;

import com.epam.ta.reportportal.auth.ReportPortalUser;
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