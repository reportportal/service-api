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

package com.epam.ta.reportportal.core.analyzer.auto.client;

import com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils;
import com.epam.ta.reportportal.ws.model.analyzer.AnalyzedItemRs;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRq;
import com.epam.ta.reportportal.ws.model.analyzer.SearchRs;

import java.util.List;
import java.util.Map;

/**
 * Rabbit client for all log indexing/analysis services. Such services are those that have
 * tag {@link AnalyzerUtils#ANALYZER_KEY}
 * in service's metadata.
 * <p>
 * To define that service indexes/collecting data it should be indicated by tag
 * {@link AnalyzerUtils#ANALYZER_INDEX}
 * with <code>true</code> in metadata. If tag is not provided it is <code>false</code> by default
 * <p>
 * Items are analyzed in order of priority specified in tag
 * * {@link AnalyzerUtils#ANALYZER_PRIORITY} in metadata.
 * If priority is not provided service gets the lowest one. If several analyzers provided different
 * issues for one item, it would be overwritten with results of more priority
 * service.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
public interface AnalyzerServiceClient {

	/**
	 * Checks if any client is available
	 *
	 * @return <code>true</code> if some exists
	 */
	boolean hasClients();

	/**
	 * Analyze launch
	 *
	 * @param rq Launch
	 * @return Analyzed Launch
	 */
	Map<String, List<AnalyzedItemRs>> analyze(IndexLaunch rq);

	/**
	 * Searches logs with similar log message
	 *
	 * @param rq {@link SearchRq} request
	 * @return {@link List<SearchRs>} of log ids
	 */
	List<SearchRs> searchLogs(SearchRq rq);

}
