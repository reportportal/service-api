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

import java.util.Map;

/**
 * Stores {@link AnalyzeItemsMode} - {@link AnalyzeItemsCollector} mapping
 *
 * @author Pavel Bortnik
 */
public class AnalyzeCollectorFactory {

	private Map<AnalyzeItemsMode, AnalyzeItemsCollector> mapping;

	public AnalyzeCollectorFactory(Map<AnalyzeItemsMode, AnalyzeItemsCollector> mapping) {
		this.mapping = mapping;
	}

	public AnalyzeItemsCollector getCollector(AnalyzeItemsMode type) {
		return this.mapping.get(type);
	}
}
