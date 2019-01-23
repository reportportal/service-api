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

package com.epam.ta.reportportal.core.analyzer.client;

import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * @author Pavel Bortnik
 */
public final class ClientUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientUtils.class);

	public static final String ANALYZER_KEY = "analyzer";
	static final String ANALYZER_PRIORITY = "analyzer_priority";
	static final String ANALYZER_INDEX = "analyzer_index";

	/**
	 * Comparing by client service priority
	 */
	static final ToIntFunction<ExchangeInfo> EXCHANGE_PRIORITY = it -> {
		try {
			return Integer.parseInt((String) it.getArguments().get(ANALYZER_PRIORITY));
		} catch (Exception e) {
			LOGGER.warn(
					"Incorrect specification of tag '{}' for service '{}'. Using the lowest priority",
					ANALYZER_PRIORITY,
					it.getArguments().get(ANALYZER_KEY),
					e
			);
			return Integer.MAX_VALUE;
		}
	};

	/**
	 * Checks if service support items indexing. <code>false</code>
	 * by default
	 */
	static final Predicate<ExchangeInfo> DOES_SUPPORT_INDEX = it -> {
		try {
			return (Boolean) it.getArguments().get(ANALYZER_INDEX);
		} catch (Exception e) {
			LOGGER.warn(
					"Incorrect specification of tag '{}' for service '{}'. Using 'false' as default value.",
					ANALYZER_INDEX,
					it.getArguments().get(ANALYZER_KEY),
					e
			);
			return false;
		}
	};

}
