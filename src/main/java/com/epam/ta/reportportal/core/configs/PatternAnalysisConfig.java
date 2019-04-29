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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.pattern.CreatePatternTemplateHandler;
import com.epam.ta.reportportal.core.pattern.impl.CreateRegexPatternTemplateHandler;
import com.epam.ta.reportportal.core.pattern.impl.CreateStringPatternTemplateHandler;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.pattern.selector.impl.RegexPatternAnalysisSelector;
import com.epam.ta.reportportal.core.pattern.selector.impl.StringPartPatternAnalysisSelector;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class PatternAnalysisConfig implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean("createPatternTemplateMapping")
	public Map<PatternTemplateType, CreatePatternTemplateHandler> createPatternTemplateHandlerMapping() {
		return ImmutableMap.<PatternTemplateType, CreatePatternTemplateHandler>builder().put(PatternTemplateType.STRING,
				applicationContext.getBean(CreateStringPatternTemplateHandler.class)
		).put(PatternTemplateType.REGEX, applicationContext.getBean(CreateRegexPatternTemplateHandler.class)).build();
	}

	@Bean("patternAnalysisSelectorMapping")
	public Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping() {
		return ImmutableMap.<PatternTemplateType, PatternAnalysisSelector>builder().put(PatternTemplateType.STRING,
				applicationContext.getBean(StringPartPatternAnalysisSelector.class)
		).put(PatternTemplateType.REGEX, applicationContext.getBean(RegexPatternAnalysisSelector.class)).build();
	}

	@Bean("patternAnalysisTaskExecutor")
	public TaskExecutor patternAnalysisTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(20);
		taskExecutor.setMaxPoolSize(100);
		taskExecutor.setQueueCapacity(600);
		taskExecutor.setThreadNamePrefix("pattern-analysis-task-exec");
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return taskExecutor;
	}

}
