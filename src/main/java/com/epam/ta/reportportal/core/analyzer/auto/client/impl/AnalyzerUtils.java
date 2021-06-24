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

package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public final class AnalyzerUtils {

	public static final String ANALYZER_KEY = "analyzer";
	static final String ANALYZER_PRIORITY = "analyzer_priority";
	static final String ANALYZER_INDEX = "analyzer_index";
	static final String ANALYZER_LOG_SEARCH = "analyzer_log_search";
	static final String ANALYZER_SUGGEST = "analyzer_suggest";

	/**
	 * Comparing by client service priority
	 */
	static final ToIntFunction<ExchangeInfo> EXCHANGE_PRIORITY = it -> ofNullable(it.getArguments()
			.get(ANALYZER_PRIORITY)).map(val -> NumberUtils.toInt(val.toString(), Integer.MAX_VALUE)).orElse(Integer.MAX_VALUE);

	/**
	 * Checks if service support items indexing. <code>false</code>
	 * by default
	 */
	static final Predicate<ExchangeInfo> DOES_SUPPORT_INDEX = it -> ofNullable(it.getArguments()
			.get(ANALYZER_INDEX)).map(val -> BooleanUtils.toBoolean(val.toString())).orElse(false);

	/**
	 * Checks if service support logs searching. <code>false</code>
	 * by default
	 */
	static final Predicate<ExchangeInfo> DOES_SUPPORT_SEARCH = it -> ofNullable(it.getArguments()
			.get(ANALYZER_LOG_SEARCH)).map(val -> BooleanUtils.toBoolean(val.toString())).orElse(false);

	/**
	 * Checks if service support logs searching. <code>false</code>
	 * by default
	 */
	static final Predicate<ExchangeInfo> DOES_SUPPORT_SUGGEST = it -> ofNullable(it.getArguments()
			.get(ANALYZER_SUGGEST)).map(val -> BooleanUtils.toBoolean(val.toString())).orElse(false);

}
