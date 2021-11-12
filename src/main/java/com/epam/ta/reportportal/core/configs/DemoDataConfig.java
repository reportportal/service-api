package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.demodata.service.generator.DefaultSuiteGenerator;
import com.epam.ta.reportportal.demodata.service.generator.SuiteGeneratorResolver;
import com.epam.ta.reportportal.demodata.service.generator.SuiteWithNestedStepsGenerator;
import com.epam.ta.reportportal.demodata.service.generator.SuiteWithRetriesGenerator;
import com.epam.ta.reportportal.demodata.service.generator.model.SuiteGeneratorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class DemoDataConfig {

	private final DefaultSuiteGenerator defaultSuiteGenerator;
	private final SuiteWithRetriesGenerator suiteWithRetriesGenerator;
	private final SuiteWithNestedStepsGenerator suiteWithNestedStepsGenerator;

	@Autowired
	public DemoDataConfig(DefaultSuiteGenerator defaultSuiteGenerator, SuiteWithRetriesGenerator suiteWithRetriesGenerator,
			SuiteWithNestedStepsGenerator suiteWithNestedStepsGenerator) {
		this.defaultSuiteGenerator = defaultSuiteGenerator;
		this.suiteWithRetriesGenerator = suiteWithRetriesGenerator;
		this.suiteWithNestedStepsGenerator = suiteWithNestedStepsGenerator;
	}

	@Bean
	public SuiteGeneratorResolver suiteGeneratorResolver() {
		return new SuiteGeneratorResolver(Map.of(SuiteGeneratorType.DEFAULT,
				defaultSuiteGenerator,
				SuiteGeneratorType.RETRY,
				suiteWithRetriesGenerator,
				SuiteGeneratorType.NESTED,
				suiteWithNestedStepsGenerator
		));
	}
}
