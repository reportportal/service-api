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

package com.epam.ta.reportportal.core.analyzer.auto.starter.decorator;

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.starter.LaunchAutoAnalysisStarter;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IndexingAutoAnalysisStarter implements LaunchAutoAnalysisStarter {

	private final GetLaunchHandler getLaunchHandler;
	private final LogIndexer logIndexer;
	private final LaunchAutoAnalysisStarter launchAutoAnalysisStarter;

	public IndexingAutoAnalysisStarter(GetLaunchHandler getLaunchHandler, LogIndexer logIndexer,
			LaunchAutoAnalysisStarter launchAutoAnalysisStarter) {
		this.getLaunchHandler = getLaunchHandler;
		this.logIndexer = logIndexer;
		this.launchAutoAnalysisStarter = launchAutoAnalysisStarter;
	}

	@Override
	@Transactional
	public void start(StartLaunchAutoAnalysisConfig config) {
		final Launch launch = getLaunchHandler.get(config.getLaunchId());
		logIndexer.indexLaunchLogs(launch, config.getAnalyzerConfig());
		launchAutoAnalysisStarter.start(config);
	}
}
