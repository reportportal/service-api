/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.events.handler.launch;

import com.epam.reportportal.extension.event.LaunchUniqueErrorAnalysisFinishEvent;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchUniqueErrorAnalysisRunner implements ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

	private final UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchUniqueErrorAnalysisRunner(@Qualifier("uniqueErrorAnalysisStarter") UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter,
			ApplicationEventPublisher eventPublisher) {
		this.uniqueErrorAnalysisStarter = uniqueErrorAnalysisStarter;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handle(LaunchFinishedEvent launchFinishedEvent, Map<String, String> projectConfig) {
		final boolean enabled = BooleanUtils.toBoolean(projectConfig.get(AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute()));
		if (enabled) {
			uniqueErrorAnalysisStarter.start(ClusterEntityContext.of(launchFinishedEvent.getId(), launchFinishedEvent.getProjectId()),
					projectConfig
			);
		}
		eventPublisher.publishEvent(new LaunchUniqueErrorAnalysisFinishEvent(launchFinishedEvent.getId()));
	}

}
