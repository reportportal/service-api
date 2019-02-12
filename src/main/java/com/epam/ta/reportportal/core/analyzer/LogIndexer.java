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

import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for indexing log content in some external storage for further search/analysis.
 *
 * @author Ivan Sharamet
 */
public interface LogIndexer {

	/**
	 * Index logs with it's level greater than
	 * {@link com.epam.ta.reportportal.entity.enums.LogLevel#ERROR}
	 * for all given test items within launch
	 *
	 * @param projectId      - project id
	 * @param launchIds      - ID of the launch
	 * @param analyzerConfig - anlayzer config
	 * @return The count of indexed test items
	 */
	CompletableFuture<Long> indexLogs(Long projectId, List<Long> launchIds, AnalyzerConfig analyzerConfig);

	/**
	 * Delete index of specified project
	 *
	 * @param project Project/index
	 */
	void deleteIndex(Long project);

	/**
	 * Remove documents with specified ids from index
	 *
	 * @param index Index to to be cleaned
	 * @param ids   Document ids to be deleted from index
	 */
	void cleanIndex(Long index, List<Long> ids);

}
