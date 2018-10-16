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
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<Set<ProjectAttribute>> implements ActivityEvent {

	private final Long updatedBy;
	private final Long projectId;
	private final String projectName;

	public ProjectAnalyzerConfigEvent(Set<ProjectAttribute> before, Set<ProjectAttribute> after, Long updatedBy, Long projectId,
			String projectName) {
		super(before, after);
		this.updatedBy = updatedBy;
		this.projectId = projectId;
		this.projectName = projectName;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_ANALYZER.getValue());
		activity.setEntity(Activity.Entity.PROJECT);
		activity.setProjectId(projectId);
		activity.setUserId(updatedBy);

		ActivityDetails details = new ActivityDetails(projectName);

		Map<String, String> oldConfig = ProjectUtils.getConfigParameters(getBefore());
		Map<String, String> newConfig = ProjectUtils.getConfigParameters(getAfter());

		processParameter(details, oldConfig, newConfig, AUTO_ANALYZER_MODE.getAttribute());
		processParameter(details, oldConfig, newConfig, MIN_DOC_FREQ.getAttribute());
		processParameter(details, oldConfig, newConfig, MIN_TERM_FREQ.getAttribute());
		processParameter(details, oldConfig, newConfig, MIN_SHOULD_MATCH.getAttribute());
		processParameter(details, oldConfig, newConfig, NUMBER_OF_LOG_LINES.getAttribute());
		processParameter(details, oldConfig, newConfig, AUTO_ANALYZER_ENABLED.getAttribute());

		activity.setDetails(details);
		return activity;
	}

	private void processParameter(ActivityDetails details, Map<String, String> oldConfig, Map<String, String> newConfig,
			String parameterName) {
		String oldValue = oldConfig.get(parameterName);
		String newValue = newConfig.get(parameterName);
		ofNullable(newValue).ifPresent(param -> {
			if (!param.equals(oldValue)) {
				details.addHistoryField(HistoryField.of(parameterName, oldValue, param));
			}
		});
	}
}
