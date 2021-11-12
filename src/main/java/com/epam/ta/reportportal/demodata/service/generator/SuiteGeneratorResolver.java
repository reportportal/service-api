package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.service.generator.model.SuiteGeneratorType;

import java.util.Map;

public class SuiteGeneratorResolver {

	private final Map<SuiteGeneratorType, SuiteGenerator> suiteGeneratorMapping;

	public SuiteGeneratorResolver(Map<SuiteGeneratorType, SuiteGenerator> suiteGeneratorMapping) {
		this.suiteGeneratorMapping = suiteGeneratorMapping;
	}

	public SuiteGenerator resolve(SuiteGeneratorType type) {
		return suiteGeneratorMapping.get(type);
	}
}
