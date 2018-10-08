/*
 * Copyright 2017 EPAM Systems
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<Set<ProjectAttribute>> implements ActivityEvent {

	private final Long updatedBy;
	private final Long projectId;

	public ProjectAnalyzerConfigEvent(Set<ProjectAttribute> before, Set<ProjectAttribute> after, Long updatedBy, Long projectId) {
		super(before, after);
		this.updatedBy = updatedBy;
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_ANALYZER.getValue());
		activity.setEntity(Activity.Entity.PROJECT);
		activity.setProjectId(projectId);
		activity.setUserId(updatedBy);

		ActivityDetails details = new ActivityDetails();

/*		Map<ProjectAttributeEnum, String> oldAnalyzerConfig = extractAnalyzerConfig(getBefore());
		Map<ProjectAttributeEnum, String> newAnalyzerConfig = extractAnalyzerConfig(getAfter());

		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, ANALYZE_MODE);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_DOC_FREQ);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_TERM_FREQ);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_SHOULD_MATCH);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, NUMBER_OF_LOG_LINES);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, AUTO_ANALYZER_ENABLED);*/

		activity.setDetails(details);
		return activity;
	}

	private void processConfigAttribute(ActivityDetails details, Map<ProjectAttributeEnum, String> oldAnalyzerConfig,
			Map<ProjectAttributeEnum, String> newAnalyzerConfig, ProjectAttributeEnum attribute) {
	/*	String oldValue = oldAnalyzerConfig.get(attribute);
		String newValue = newAnalyzerConfig.get(attribute);
		if (null != newValue && !newValue.equalsIgnoreCase(oldValue)) {
			details.addHistoryField(attribute.getValue(), new HistoryField(oldValue, newValue));
		}
	}

	private Map<ProjectAttributeEnum, String> extractAnalyzerConfig(Set<ProjectAttribute> attributes) {
		Map<ProjectAttributeEnum, String> analyzerConfigAttributes = new HashMap<>();

		extractConfigProperty(attributes, analyzerConfigAttributes, ANALYZE_MODE);
		extractConfigProperty(attributes, analyzerConfigAttributes, MIN_DOC_FREQ);
		extractConfigProperty(attributes, analyzerConfigAttributes, MIN_TERM_FREQ);
		extractConfigProperty(attributes, analyzerConfigAttributes, MIN_SHOULD_MATCH);
		extractConfigProperty(attributes, analyzerConfigAttributes, NUMBER_OF_LOG_LINES);
		extractConfigProperty(attributes, analyzerConfigAttributes, AUTO_ANALYZER_ENABLED);
		return analyzerConfigAttributes;
	}

	private void extractConfigProperty(Set<ProjectAttribute> attributes, Map<ProjectAttributeEnum, String> analyzerConfigAttributes,
			ProjectAttributeEnum attributeEnum) {
		attributes.stream()
				.filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(attributeEnum.getValue()))
				.findFirst()
				.ifPresent(attr -> analyzerConfigAttributes.put(attributeEnum, attr.getValue()));
				*/
	}
}
