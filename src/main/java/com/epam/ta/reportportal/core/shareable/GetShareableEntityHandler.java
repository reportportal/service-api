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

package com.epam.ta.reportportal.core.shareable;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.ShareableEntity;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface GetShareableEntityHandler<T extends ShareableEntity> {

	/**
	 * Get {@link ShareableEntity} on which user have {@link com.epam.ta.reportportal.auth.permissions.AclReadPermission} by id
	 *
	 * @param id             {@link ShareableEntity#id}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return dashboard
	 */
	T getPermitted(Long id, ReportPortalUser.ProjectDetails projectDetails);

	/**
	 * Get {@link ShareableEntity} on which user have {@link com.epam.ta.reportportal.auth.permissions.AclFullPermission} by id
	 *
	 * @param id             {@link ShareableEntity#id}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return dashboard
	 */
	T getAdministrated(Long id, ReportPortalUser.ProjectDetails projectDetails);

}
