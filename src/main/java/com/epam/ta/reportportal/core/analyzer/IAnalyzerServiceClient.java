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

package com.epam.ta.reportportal.core.analyzer;

import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;

import java.util.List;
import java.util.Map;

/**
 * HTTP client for all log indexing/analysis services. Such services are those that have
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
public interface IAnalyzerServiceClient {

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
	List<IndexRs> index(List<IndexLaunch> rq);

	/**
	 * Analyze launch
	 *
	 * @param rq Launch
	 * @return Analyzed Launch
	 */
	Map<String, List<AnalyzedItemRs>> analyze(IndexLaunch rq);

	/**
	 * Remove documents with specified ids from index
	 *
	 * @param index Index to to be cleaned
	 * @param ids   Document ids to be deleted from index
	 */
	void cleanIndex(String index, List<String> ids);

	/**
	 * Delete index
	 *
	 * @param index Index to be deleted
	 */
	void deleteIndex(String index);
}
