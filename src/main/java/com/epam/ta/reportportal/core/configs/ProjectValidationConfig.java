package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.project.validator.attribute.DelayBoundLessRule;
import com.epam.ta.reportportal.core.project.validator.attribute.DelayBoundValidator;
import com.epam.ta.reportportal.core.project.validator.attribute.ProjectAttributeValidator;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProjectValidationConfig {

	@Bean
	public DelayBoundValidator delayBoundValidator() {
		return new DelayBoundValidator(getDelayBoundRules());
	}

	private List<DelayBoundLessRule> getDelayBoundRules() {
		return List.of(new DelayBoundLessRule(ProjectAttributeEnum.KEEP_SCREENSHOTS, ProjectAttributeEnum.KEEP_LOGS),
				new DelayBoundLessRule(ProjectAttributeEnum.KEEP_LOGS, ProjectAttributeEnum.KEEP_LAUNCHES)
		);
	}

	@Bean
	public ProjectAttributeValidator projectAttributeValidator() {
		return new ProjectAttributeValidator(delayBoundValidator());
	}
}
