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

package com.epam.ta.reportportal.core.remover.project;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.entity.project.Project;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ProjectContentRemover implements ContentRemover<Project> {

	private final List<ContentRemover<Project>> removers;

	public ProjectContentRemover(List<ContentRemover<Project>> removers) {
		this.removers = removers;
	}

	@Override
	public void remove(Project project) {
		removers.forEach(r -> r.remove(project));
	}
}
