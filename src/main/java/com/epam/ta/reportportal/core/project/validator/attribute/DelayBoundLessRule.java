package com.epam.ta.reportportal.core.project.validator.attribute;

import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;

public class DelayBoundLessRule {

	private final ProjectAttributeEnum lower;
	private final ProjectAttributeEnum higher;

	public DelayBoundLessRule(ProjectAttributeEnum lower, ProjectAttributeEnum higher) {
		this.lower = lower;
		this.higher = higher;
	}

	public ProjectAttributeEnum getLower() {
		return lower;
	}

	public ProjectAttributeEnum getHigher() {
		return higher;
	}
}
