/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.analyzer.auto.client;

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLaunch;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Client for the external log-indexer (index, clean, defect updates).
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface IndexerServiceClient {

  /**
   * Sends a request to remove documents with specified ids from index
   *
   * @param index Index to to be cleaned
   * @param ids   Document ids to be deleted from index
   */
  void cleanIndex(Long index, List<Long> ids);

  /**
   * Delete index and wait for analyzer response.
   *
   * @param index Index to be deleted
   */
  void deleteIndex(Long index);

  /**
   * Sends a request to delete index without waiting for analyzer response.
   *
   * @param index Index to be deleted
   */
  void deleteIndexAsync(Long index);

  /**
   * Index list of launches
   *
   * @param rq Launches
   */
  void index(List<IndexLaunch> rq);

  /**
   * Sends a message to the queue with a map of items which must be updated with a new issue type
   *
   * @param projectId           Project id
   * @param itemsForIndexUpdate Pair of itemId - issue type
   * @param autoAnalyzed        {@code true} if the update was triggered by auto-analysis,
   *                            {@code false} for manual user updates
   * @return List of missed items in analyzer
   */
  List<Long> indexDefectsUpdate(Long projectId, Map<Long, String> itemsForIndexUpdate, boolean autoAnalyzed);

  /**
   * Sends a message to the queue with a list of items which must be removed from index
   *
   * @param projectId           Project id
   * @param itemsForIndexRemove List of item ids
   */
  void indexItemsRemoveAsync(Long projectId, Collection<Long> itemsForIndexRemove);

  /**
   * Sends a message to the queue with a list of launches which must be removed from index
   *
   * @param projectId              Project id
   * @param launchesForIndexRemove List of launhces ids
   */
  void indexLaunchesRemove(Long projectId, Collection<Long> launchesForIndexRemove);

}
