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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * GET operations for {@link TestItem}
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
public interface GetTestItemHandler {
	/**
	 * Get {@link TestItem} instance
	 *
	 * @param testItemId     {@link TestItem#itemId}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param user           {@link ReportPortalUser}
	 * @return {@link TestItemResource}
	 */
	TestItemResource getTestItem(Long testItemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Gets {@link TestItem} instances
	 *
	 * @param filter         {@link Filter}
	 * @param pageable       {@link Pageable}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param user           {@link ReportPortalUser}
	 * @return {@link Iterable} of the {@link TestItemResource}
	 */
	Iterable<TestItemResource> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, Long launchId);

	/**
	 * Get specified attribute keys
	 *
	 * @param launchId {@link com.epam.ta.reportportal.entity.launch.Launch#id}
	 * @param value    part of the {@link com.epam.ta.reportportal.entity.ItemAttribute#key} to search
	 * @return {@link List} of the {@link com.epam.ta.reportportal.entity.ItemAttribute#key}
	 */
	List<String> getAttributeKeys(Long launchId, String value);

	/**
	 * Get specified attribute values
	 *
	 * @param launchId {@link com.epam.ta.reportportal.entity.launch.Launch#id}
	 * @param value    part of the {@link com.epam.ta.reportportal.entity.ItemAttribute#value} to search
	 * @return {@link List} of the {@link com.epam.ta.reportportal.entity.ItemAttribute#value}
	 */
	List<String> getAttributeValues(Long launchId, String key, String value);

	/**
	 * @param ids array of the {@link com.epam.ta.reportportal.entity.launch.Launch#id}
	 * @return {@link List} of the {@link TestItemResource}
	 */
	List<TestItemResource> getTestItems(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);
}
