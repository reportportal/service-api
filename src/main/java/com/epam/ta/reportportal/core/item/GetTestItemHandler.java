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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.querygen.Filter;
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
	 * @param testItemId
	 * @return
	 */
	TestItemResource getTestItem(Long testItemId);

	/**
	 * Gets {@link TestItem} instances
	 *
	 * @param filter
	 * @param pageable
	 * @return
	 */
	Iterable<TestItemResource> getTestItems(Filter filter, Pageable pageable);

	/**
	 * Get specified tags
	 *
	 * @param launchId
	 * @param value
	 * @return
	 */
	List<String> getTags(Long launchId, String value);

	List<TestItemResource> getTestItems(Long[] ids);
}
