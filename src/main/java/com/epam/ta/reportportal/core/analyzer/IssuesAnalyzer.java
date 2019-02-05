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

package com.epam.ta.reportportal.core.analyzer;

import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for issue type analysis based on historical data.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
public interface IssuesAnalyzer {

	/**
	 * Analyze history to find similar issues and updates items if some were found
	 * Indexes investigated issues as well.
	 *
	 * @param launch         - Initial launch for history
	 * @param testItemIds    - Prepared ids of test item for analyzing
	 * @param analyzerConfig - Analyze mode
	 */
	CompletableFuture<Void> analyze(Launch launch, List<Long> testItemIds, AnalyzerConfig analyzerConfig);

	/**
	 * Checks if any analyzer is available
	 *
	 * @return <code>true</code> if some exists
	 */
	boolean hasAnalyzers();
}
