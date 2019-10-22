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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Contains caches for analyzing and indexing status
 *
 * @author Pavel Bortnik
 */
@Service
public class AnalyzerStatusCache {

	public static final String AUTO_ANALYZER_KEY = "autoAnalyzer";
	public static final String PATTERN_ANALYZER_KEY = "patternAnalyzer";

	private static final int CACHE_ITEM_LIVE = 10;
	private static final int MAXIMUM_SIZE = 50000;

	/**
	 * Contains cache of analyze running for concrete launch
	 * launchId - projectId
	 */
	private Map<String, Cache<Long, Long>> analyzeStatus;

	public AnalyzerStatusCache() {
		Cache<Long, Long> autoAnalysisStatusCache = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_SIZE)
				.expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES)
				.build();
		Cache<Long, Long> patternAnalysisCache = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_SIZE)
				.expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES)
				.build();
		analyzeStatus = ImmutableMap.<String, Cache<Long, Long>>builder().put(AUTO_ANALYZER_KEY, autoAnalysisStatusCache)
				.put(PATTERN_ANALYZER_KEY, patternAnalysisCache)
				.build();
	}

	public AnalyzerStatusCache(Map<String, Cache<Long, Long>> analyzeStatus) {
		this.analyzeStatus = analyzeStatus;
	}

	public boolean analyzeStarted(String analyzerKey, Long launchId, Long projectId) {
		Cache<Long, Long> analysisCache = analyzeStatus.get(analyzerKey);
		if (analysisCache == null) {
			return false;
		}
		analysisCache.put(launchId, projectId);
		return true;
	}

	public boolean analyzeFinished(String analyzerKey, Long launchId) {
		Cache<Long, Long> analysisCache = analyzeStatus.get(analyzerKey);
		if (analysisCache == null) {
			return false;
		}
		analysisCache.invalidate(launchId);
		return true;
	}

	public Optional<Cache<Long, Long>> getAnalyzeStatus(String analyzerKey) {
		return ofNullable(analyzeStatus.get(analyzerKey));
	}

	public Set<String> getStartedAnalyzers(Long launchId) {
		return analyzeStatus.entrySet()
				.stream()
				.filter(entry -> entry.getValue().asMap().containsKey(launchId))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	public Set<String> getAnalyzers() {
		return analyzeStatus.keySet();
	}
}
