/*
 * Copyright 2017 EPAM Systems
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
 *
 */

package com.epam.ta.reportportal.core.analyzer.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Pavel Bortnik
 */
@Service
public class AnalyzerStatusCache {

	private static final int CACHE_ITEM_LIVE = 1440;
	private static final int MAXIMUM_SIZE = 10000;

	private LoadingCache<String, String> analyzerStatus;

	public AnalyzerStatusCache() {
		analyzerStatus = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_SIZE)
				.expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>() {
					@Override
					public String load(String key) throws Exception {
						return null;
					}
				});
	}

	public void analyzeStarted(String launchId, String projectName) {
		analyzerStatus.put(launchId, projectName);
	}

	public void analyzeFinished(String launchId) {
		analyzerStatus.invalidate(launchId);
	}

	public LoadingCache<String, String> getAnalyzerStatus() {
		return analyzerStatus;
	}
}
