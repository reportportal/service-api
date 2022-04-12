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

package com.epam.ta.reportportal.core.events.listener;

import com.epam.reportportal.extension.event.LaunchStartUniqueErrorAnalysisEvent;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.project.config.ProjectConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class StartLaunchUniqueErrorAnalysisEventListener {

	private final ProjectConfigProvider projectConfigProvider;
	private final UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter;

	@Autowired
	public StartLaunchUniqueErrorAnalysisEventListener(ProjectConfigProvider projectConfigProvider,
			@Qualifier("uniqueErrorAnalysisStarter") UniqueErrorAnalysisStarter uniqueErrorAnalysisStarter) {
		this.projectConfigProvider = projectConfigProvider;
		this.uniqueErrorAnalysisStarter = uniqueErrorAnalysisStarter;
	}

	@EventListener
	public void onApplicationEvent(LaunchStartUniqueErrorAnalysisEvent event) {
		final Map<String, String> projectConfig = projectConfigProvider.provide(event.getProjectId());
		uniqueErrorAnalysisStarter.start(ClusterEntityContext.of(event.getSource(), event.getProjectId()), projectConfig);
	}
}
