package com.epam.ta.reportportal.core.analyzer.auto.indexer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class IndexerStatusCache {

	private static final int CACHE_ITEM_LIVE = 10;
	private static final int MAXIMUM_SIZE = 50000;

	/**
	 * Contains cache of indexing running for concrete project
	 * launchId - projectId
	 */
	private Cache<Long, Boolean> indexingStatus;

	public IndexerStatusCache() {
		indexingStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
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
