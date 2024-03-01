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

package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class IndexerStatusCache {

  private static final int CACHE_ITEM_LIVE = 10;
  private static final int MAXIMUM_SIZE = 50000;

  /**
   * Contains cache of indexing running for concrete project launchId - projectId
   */
  private Cache<Long, Boolean> indexingStatus;

  public IndexerStatusCache() {
    indexingStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE)
        .expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
  }

  public void indexingStarted(Long projectId) {
    indexingStatus.put(projectId, true);
  }

  public void indexingFinished(Long projectId) {
    indexingStatus.invalidate(projectId);
  }

  public Cache<Long, Boolean> getIndexingStatus() {
    return indexingStatus;
  }
}
