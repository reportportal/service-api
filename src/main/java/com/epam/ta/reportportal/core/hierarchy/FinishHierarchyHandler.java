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

package com.epam.ta.reportportal.core.hierarchy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.StatusEnum;

import java.util.Date;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface FinishHierarchyHandler<T> {

	/**
	 * @param parentEntity   Parent entity which descendants should be finished
	 * @param status         {@link StatusEnum} that should be assigned to descendants
	 * @param endDate        {@link java.time.LocalDateTime} finish date for descendants
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @return finished descendants count
	 */
	int finishDescendants(T parentEntity, StatusEnum status, Date endDate, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails);
}
