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
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.UPDATE_ANALYZER;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<ProjectAttributesActivityResource> implements ActivityEvent {

	private Long updatedBy;

	public ProjectAnalyzerConfigEvent() {
	}

	public ProjectAnalyzerConfigEvent(ProjectAttributesActivityResource before, ProjectAttributesActivityResource after, Long updatedBy) {
		super(before, after);
		this.updatedBy = updatedBy;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_ANALYZER)
				.addActivityEntityType(PROJECT)
				.addUserId(updatedBy)
				.addObjectId(getAfter().getProjectId())
				.addObjectName(getAfter().getProjectName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), AUTO_ANALYZER_MODE.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_DOC_FREQ.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_TERM_FREQ.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_SHOULD_MATCH.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), NUMBER_OF_LOG_LINES.getAttribute()))
				.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), AUTO_ANALYZER_ENABLED.getAttribute()))
				.get();
	}

	static Optional<HistoryField> processParameter(Map<String, String> oldConfig, Map<String, String> newConfig, String parameterName) {
		String before = oldConfig.get(parameterName);
		String after = newConfig.get(parameterName);
		if (after != null && !after.equals(before)) {
			return Optional.of(HistoryField.of(parameterName, before, after));
		}
		return Optional.empty();
	}
}
