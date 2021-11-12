/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.cluster.utils;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ConfigProvider {

	private ConfigProvider() {

	}

	public static final  GenerateClustersConfig getConfig(boolean forUpdate) {
		final GenerateClustersConfig config = new GenerateClustersConfig();
		final AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setNumberOfLogLines(1);
		config.setAnalyzerConfig(analyzerConfig);
		config.setProject(1L);
		config.setLaunchId(1L);
		config.setForUpdate(forUpdate);
		config.setCleanNumbers(false);
		return config;
	}
}
