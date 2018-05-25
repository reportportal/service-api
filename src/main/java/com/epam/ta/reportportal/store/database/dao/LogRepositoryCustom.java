/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.log.Log;
import java.util.List;

/**
 * @author Pavel Bortnik
 */
public interface LogRepositoryCustom  extends FilterableRepository<Log>{

	/**
	 * Checks if the test item has any logs.
	 *
	 * @param itemId Item id
	 * @return true if logs were found
	 */
	boolean hasLogs(Long itemId);

	/**
	 * Load specified number of last logs for specified test item. binaryData
	 * field will be loaded if it specified in appropriate input parameter, all
	 * other fields will be fully loaded.
	 *
	 * @param itemId
	 * @param limit
	 * @param isLoadBinaryData
	 * @return
	 */
	List<Log> findByTestItemId(String itemId, int limit, boolean isLoadBinaryData);
}
