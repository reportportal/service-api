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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Contains cache of analyzing launches. Key is a launch id,
 * value is a project name.
 *
 * @author Pavel Bortnik
 */
@Service
public class AnalyzerStatusCache {

	private static final int CACHE_ITEM_LIVE = 100;
	private static final int MAXIMUM_SIZE = 10000;

	private Cache<Long, Long> analyzerStatus;
	private static final Logger LOGGER = LogManager.getLogger(AnalyzerStatusCache.class.getSimpleName());

	public AnalyzerStatusCache() {
		analyzerStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
	}

	public void analyzeStarted(Long launchId, Long projectId) {
		analyzerStatus.put(launchId, projectId);
	}

	public void analyzeFinished(Long launchId) {
		LOGGER.error("I am invalidating analyzer status cache");
		analyzerStatus.invalidate(launchId);
	}

	public Cache<Long, Long> getAnalyzerStatus() {
		return analyzerStatus;
	}
}
