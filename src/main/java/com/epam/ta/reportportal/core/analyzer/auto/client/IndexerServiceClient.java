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

import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface IndexerServiceClient {

	/**
	 * Remove documents with specified ids from index
	 *
	 * @param index Index to to be cleaned
	 * @param ids   Document ids to be deleted from index
	 * @return Amount of deleted logs
	 */
	Long cleanIndex(Long index, List<Long> ids);

	/**
	 * Delete index
	 *
	 * @param index Index to be deleted
	 */
	void deleteIndex(Long index);

	/**
	 * Index list of launches
	 *
	 * @param rq Launches
	 * @return Count of indexed test items
	 */
	Long index(List<IndexLaunch> rq);

	/**
	 * Sends a message to the queue with a map of items which must be updated with a new issue type
	 *
	 * @param itemsForIndexUpdate Pair of itemId - issue type
	 * @return List of missed items in analyzer
	 */
	List<Long> indexDefectsUpdate(Long projectId, Map<Long, String> itemsForIndexUpdate);

	/**
	 * Sends a message to the queue with a list of items which must be removed from index
	 * and receive number of removed objects as a response.
	 *
	 * @param itemsForIndexRemove List of item ids
	 * @return number of removed objects
	 */
	Integer indexItemsRemove(Long projectId, Collection<Long> itemsForIndexRemove);

	/**
	 * Sends a message to the queue with a list of items which must be removed from index
	 *
	 * @param itemsForIndexRemove List of item ids
	 */
	void indexItemsRemoveAsync(Long projectId, Collection<Long> itemsForIndexRemove);

	/**
	 * Sends a message to the queue with a list of launches which must be removed from index
	 *
	 * @param launchesForIndexRemove List of launhces ids
	 */
	void indexLaunchesRemove(Long projectId, Collection<Long> launchesForIndexRemove);

}
