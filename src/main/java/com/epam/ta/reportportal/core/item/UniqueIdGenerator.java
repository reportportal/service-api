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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.database.entity.item.TestItem;

/**
 * Unique id generator for designate test item's originality
 *
 * @author Pavel_Bortnik
 * @since V3.2
 */
public interface UniqueIdGenerator {

	/**
	 * Generates the unique identifier for test item
	 *
	 * @param testItem source for id
	 * @return unique id
	 */
	String generate(TestItem testItem);

	/**
	 * Validate if string has been generated automatically
	 *
	 * @param encoded encoded
	 * @return true if it has been generated automatically
	 */
	boolean validate(String encoded);

}
