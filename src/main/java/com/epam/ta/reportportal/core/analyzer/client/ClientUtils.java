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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * @author Pavel Bortnik
 */
public final class ClientUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientUtils.class);

	static final String ANALYZER_KEY = "analyzer";
	static final String ANALYZER_PRIORITY = "analyzer_priority";
	static final String ANALYZER_INDEX = "analyzer_index";

	/**
	 * Comparing by client service priority
	 */
	static final ToIntFunction<ServiceInstance> SERVICE_PRIORITY = it -> {
		try {
			return Integer.parseInt(it.getMetadata().get(ANALYZER_PRIORITY));
		} catch (Exception e) {
			LOGGER.warn("Incorrect specification of tag '{}' for service '{}'. Using the lowest priority", ANALYZER_PRIORITY,
					it.getMetadata().get(ANALYZER_KEY), e
			);
			return Integer.MAX_VALUE;
		}
	};

	/**
	 * Checks if service support items indexing. <code>false</code>
	 * by default
	 */
	static final Predicate<ServiceInstance> SUPPORT_INDEX = it -> {
		try {
			return Boolean.valueOf(it.getMetadata().get(ANALYZER_INDEX));
		} catch (Exception e) {
			LOGGER.warn("Incorrect specification of tag '{}' for service '{}'. Using 'false' as default value.", ANALYZER_INDEX,
					it.getMetadata().get(ANALYZER_KEY), e
			);
			return false;
		}
	};

}
