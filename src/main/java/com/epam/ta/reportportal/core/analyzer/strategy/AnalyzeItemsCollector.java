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

package com.epam.ta.reportportal.core.analyzer.strategy;

import java.util.List;

/**
 * @author Pavel Bortnik
 */
@FunctionalInterface
public interface AnalyzeItemsCollector {

	/**
	 * Collects items for concrete project of concrete launch for following analyzing
	 * according to concrete {@link com.epam.ta.reportportal.entity.AnalyzeMode}
	 *
	 * @param projectId Project id
	 * @param launchId  Launch id
	 * @param login     Username
	 * @return List of item ids
	 */
	List<Long> collectItems(Long projectId, Long launchId, String login);

}
