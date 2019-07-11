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

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.configEquals;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_ANALYZER;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<ProjectAttributesActivityResource> implements ActivityEvent {

	public ProjectAnalyzerConfigEvent() {
	}

	public ProjectAnalyzerConfigEvent(ProjectAttributesActivityResource before, ProjectAttributesActivityResource after, Long userId,
			String userLogin) {
		super(userId, userLogin, before, after);
	}

	@Override
	public Activity toActivity() {
		return configEquals(getBefore().getConfig(), getAfter().getConfig(), Prefix.ANALYZER) ?
				null :
				new ActivityBuilder().addCreatedNow()
						.addAction(UPDATE_ANALYZER)
						.addActivityEntityType(PROJECT)
						.addUserId(getUserId())
						.addUserName(getUserLogin())
						.addObjectId(getAfter().getProjectId())
						.addObjectName(getAfter().getProjectName())
						.addProjectId(getAfter().getProjectId())
						.addHistoryField(processParameter(getBefore().getConfig(),
								getAfter().getConfig(),
								AUTO_ANALYZER_MODE.getAttribute()
						))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_DOC_FREQ.getAttribute()))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_TERM_FREQ.getAttribute()))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), MIN_SHOULD_MATCH.getAttribute()))
						.addHistoryField(processParameter(getBefore().getConfig(),
								getAfter().getConfig(),
								NUMBER_OF_LOG_LINES.getAttribute()
						))
						.addHistoryField(processParameter(getBefore().getConfig(),
								getAfter().getConfig(),
								AUTO_ANALYZER_ENABLED.getAttribute()
						))
						.get();
	}

}
