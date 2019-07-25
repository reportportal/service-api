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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;

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
	String generate(TestItem testItem, Launch launch);

	/**
	 * Validate if string has been generated automatically
	 *
	 * @param encoded encoded
	 * @return true if it has been generated automatically
	 */
	boolean validate(String encoded);

}
