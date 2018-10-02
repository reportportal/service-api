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
import com.epam.ta.reportportal.core.events.activity.details.ActivityDetails;
import com.epam.ta.reportportal.core.events.activity.details.HistoryField;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<Set<ProjectAttribute>> implements ActivityEvent {

	private static final String ANALYZE_MODE_FIELD = "analyze_mode";
	private static final String AUTO_ANALYZE_FIELD = "auto_analyze";
	private static final String MIN_DOC_FREQ_FIELD = "min_doc_freq";
	private static final String MIN_TERM_FREQ_FIELD = "min_term_freq";
	private static final String MIN_SHOULD_MATCH_FIELD = "min_should_match";
	private static final String NUMBER_OF_LOG_LINES_FIELD = "number_of_log_lines";

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

		Map<ProjectAttributeEnum, String> oldAnalyzerConfig = extractAnalyzerConfig(getBefore());
		Map<ProjectAttributeEnum, String> newAnalyzerConfig = extractAnalyzerConfig(getAfter());

		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, ANALYZE_MODE, ANALYZE_MODE_FIELD);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_DOC_FREQ, MIN_DOC_FREQ_FIELD);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_TERM_FREQ, MIN_TERM_FREQ_FIELD);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, MIN_SHOULD_MATCH, MIN_SHOULD_MATCH_FIELD);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, NUMBER_OF_LOG_LINES, NUMBER_OF_LOG_LINES_FIELD);
		processConfigAttribute(details, oldAnalyzerConfig, newAnalyzerConfig, AUTO_ANALYZER_ENABLED, AUTO_ANALYZE_FIELD);

		activity.setDetails(details);
		return activity;
	}

	private void processConfigAttribute(ActivityDetails details, Map<ProjectAttributeEnum, String> oldAnalyzerConfig,
			Map<ProjectAttributeEnum, String> newAnalyzerConfig, ProjectAttributeEnum attribute, String field) {
		String oldValue = oldAnalyzerConfig.get(attribute);
		String newValue = newAnalyzerConfig.get(attribute);
		if (null != newValue && !newValue.equalsIgnoreCase(oldValue)) {
			details.addHistoryField(field, new HistoryField(oldValue, newValue));
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
	}
}
