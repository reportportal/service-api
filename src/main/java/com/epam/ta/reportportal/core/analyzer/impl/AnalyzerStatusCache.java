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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

	private Cache<Long, String> analyzerStatus;

	public AnalyzerStatusCache() {
		analyzerStatus = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CACHE_ITEM_LIVE, TimeUnit.MINUTES).build();
	}

	public void analyzeStarted(Long launchId, String projectName) {
		analyzerStatus.put(launchId, projectName);
	}

	public void analyzeFinished(Long launchId) {
		analyzerStatus.invalidate(launchId);
	}

	public Cache<Long, String> getAnalyzerStatus() {
		return analyzerStatus;
	}
}
