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

package com.epam.ta.reportportal.core.analyzer.client;

import com.google.common.base.Strings;
import org.springframework.cloud.client.ServiceInstance;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * @author Pavel Bortnik
 */
final class ClientUtils {

	private static final String PRIORITY = "analyzer_priority";
	private static final String ANALYZER_INDEX = "analyzer_index";

	/**
	 * Comparing by client service priority
	 */
	static final ToIntFunction<ServiceInstance> SERVICE_PRIORITY = it -> {
		String priority = it.getMetadata().get(PRIORITY);
		if (priority != null) {
			return Integer.parseInt(priority);
		}
		return Integer.MAX_VALUE;
	};

	/**
	 * Checks if service support items indexing. <code>false</code>
	 * by default
	 */
	static final Predicate<ServiceInstance> DOES_NEED_INDEX = it -> {
		String index = it.getMetadata().get(ANALYZER_INDEX);
		if (!Strings.isNullOrEmpty(index)) {
			return Boolean.valueOf(index);
		}
		return true;
	};

}
