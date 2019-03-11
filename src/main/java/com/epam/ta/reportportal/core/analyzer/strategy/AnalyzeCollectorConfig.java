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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class AnalyzeCollectorConfig implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean(name = "analyzerModeMapping")
	public Map<AnalyzeItemsMode, AnalyzeItemsCollector> getAnalyzerModeMapping() {
		Map<AnalyzeItemsMode, AnalyzeItemsCollector> mapping = new HashMap<>();
		mapping.put(AnalyzeItemsMode.TO_INVESTIGATE, applicationContext.getBean(ToInvestigateCollector.class));
		mapping.put(AnalyzeItemsMode.AUTO_ANALYZED, applicationContext.getBean(ToInvestigateCollector.class));
		mapping.put(AnalyzeItemsMode.MANUALLY_ANALYZED, applicationContext.getBean(ToInvestigateCollector.class));
		return mapping;
	}

	@Bean
	public AnalyzeCollectorFactory analyzeCollectorFactory() {
		return new AnalyzeCollectorFactory(getAnalyzerModeMapping());
	}
}
