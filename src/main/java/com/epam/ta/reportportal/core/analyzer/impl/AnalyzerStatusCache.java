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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Contains caches for analyzing and indexing status
 *
 * @author Pavel Bortnik
 */
@Service
public class AnalyzerStatusCache {

	private static final int CACHE_ITEM_LIVE = 10;
	private static final int MAXIMUM_SIZE = 50000;

	/**
	 * Contains cache of analyze running for concrete launch
	 * launchId - projectId
	 */
	private Cache<Long, Long> analyzeStatus;

	/**
	 * Contains cache of indexing running for concrete project
	 * launchId - projectId
	 */
	private Cache<Long, Boolean> indexingStatus;

	public AnalyzerStatusCache() {
		analyzeStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
		indexingStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
	}

	public void indexingStarted(Long projectId) {
		indexingStatus.put(projectId, true);
	}

	public void indexingFinished(Long projectId) {
		indexingStatus.invalidate(projectId);
	}

	public void analyzeStarted(Long launchId, Long projectId) {
		analyzeStatus.put(launchId, projectId);
	}

	public void analyzeFinished(Long launchId) {
		analyzeStatus.invalidate(launchId);
	}

	public Cache<Long, Long> getAnalyzeStatus() {
		return analyzeStatus;
	}

	public Cache<Long, Boolean> getIndexingStatus() {
		return indexingStatus;
	}
}
