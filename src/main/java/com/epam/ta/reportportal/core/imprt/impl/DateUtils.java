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
package com.epam.ta.reportportal.core.imprt.impl;

/**
 * @author Pavel Bortnik
 */
public final class DateUtils {

	private DateUtils() {
		//static only
	}

	/**
	 * Converts string representation of seconds to millis
	 *
	 * @param duration String seconds
	 * @return long millis
	 */
	public static long toMillis(String duration) {
		if (null != duration) {
			Double value = Double.valueOf(duration) * 1000;
			return value.longValue();
		}
		return 0;
	}

}
