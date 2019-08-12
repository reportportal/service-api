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

package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.ta.reportportal.core.analyzer.strategy.LaunchAnalysisStrategy;
import com.epam.ta.reportportal.core.analyzer.strategy.LaunchAutoAnalysisStrategy;
import com.epam.ta.reportportal.core.analyzer.strategy.LaunchPatternAnalysisStrategy;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class AnalyzersConfig {

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public Map<AnalyzerType, LaunchAnalysisStrategy> launchAnalysisStrategyMapping() {
		return ImmutableMap.<AnalyzerType, LaunchAnalysisStrategy>builder().put(AnalyzerType.AUTO_ANALYZER,
				applicationContext.getBean(LaunchAutoAnalysisStrategy.class)
		).put(AnalyzerType.PATTERN_ANALYZER, applicationContext.getBean(LaunchPatternAnalysisStrategy.class)).build();
	}
}
