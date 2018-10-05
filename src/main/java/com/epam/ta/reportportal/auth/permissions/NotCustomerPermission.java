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

package com.epam.ta.reportportal.auth.permissions;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import org.springframework.stereotype.Component;

/**
 * Validates this is {@link ProjectRole#MEMBER} or higher authority in the
 * authentication context
 *
 * @author Andrei Varabyeu
 */
@Component
@LookupPermission({ "notCustomerPermission" })
public class NotCustomerPermission extends BaseProjectPermission {

	/**
	 * Validates this is not a {@link ProjectRole#CUSTOMER} or higher authority in the
	 * authentication context
	 */
	@Override
	protected boolean checkAllowed(ReportPortalUser user, String project, ProjectRole role) {
		return (null != role) && role.compareTo(ProjectRole.CUSTOMER) != 0;
	}

}