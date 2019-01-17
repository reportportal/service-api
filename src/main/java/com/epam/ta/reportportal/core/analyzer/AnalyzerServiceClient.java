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

package com.epam.ta.reportportal.core.analyzer;

import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Rabbit client for all log indexing/analysis services. Such services are those that have
 * tag {@link com.epam.ta.reportportal.core.analyzer.client.ClientUtils#ANALYZER_KEY}
 * in service's metadata.
 * <p>
 * To define that service indexes/collecting data it should be indicated by tag
 * {@link com.epam.ta.reportportal.core.analyzer.client.ClientUtils#ANALYZER_INDEX}
 * with <code>true</code> in metadata. If tag is not provided it is <code>false</code> by default
 * <p>
 * Items are analyzed in order of priority specified in tag
 * * {@link com.epam.ta.reportportal.core.analyzer.client.ClientUtils#ANALYZER_PRIORITY} in metadata.
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
	 * Index list of launches
	 *
	 * @param rq Launch
	 * @return Indexing result
	 */
	List<CompletableFuture<IndexRs>> index(List<IndexLaunch> rq);

	/**
	 * Analyze launch
	 *
	 * @param rq Launch
	 * @return Analyzed Launch
	 */
	CompletableFuture<Map<String, List<AnalyzedItemRs>>> analyze(IndexLaunch rq);

	/**
	 * Remove documents with specified ids from index
	 *
	 * @param index Index to to be cleaned
	 * @param ids   Document ids to be deleted from index
	 */
	void cleanIndex(Long index, List<Long> ids);

	/**
	 * Delete index
	 *
	 * @param index Index to be deleted
	 */
	void deleteIndex(Long index);
}
