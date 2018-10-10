/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.entity.bts.DefectFormField;
import com.epam.ta.reportportal.entity.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Pavel Bortnik
 */
public class BugTrackingSystemBuilder implements Supplier<BugTrackingSystem> {

	private BugTrackingSystem bugTrackingSystem;

	public BugTrackingSystemBuilder() {
		bugTrackingSystem = new BugTrackingSystem();
	}

	public BugTrackingSystemBuilder(BugTrackingSystem bugTrackingSystem) {
		this.bugTrackingSystem = bugTrackingSystem;
	}

	public BugTrackingSystemBuilder addUrl(String url) {
		if (!StringUtils.isEmpty(url)) {
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
		}
		bugTrackingSystem.setUrl(url);
		return this;
	}

	public BugTrackingSystemBuilder addBugTrackingSystemType(String systemType) {
		if (!StringUtils.isEmpty(systemType)) {
			bugTrackingSystem.setBtsType(systemType);
		}
		return this;
	}

	public BugTrackingSystemBuilder addBugTrackingProject(String project) {
		if (!StringUtils.isEmpty(project)) {
			bugTrackingSystem.setBtsProject(project);
		}
		return this;
	}

	public BugTrackingSystemBuilder addProject(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		bugTrackingSystem.setProject(project);
		return this;
	}

	public BugTrackingSystemBuilder addFields(Set<DefectFormField> fields) {
		if (!CollectionUtils.isEmpty(fields)) {
			bugTrackingSystem.getDefectFormFields().clear();
			bugTrackingSystem.getDefectFormFields().addAll(fields);
			bugTrackingSystem.getDefectFormFields().forEach(it -> it.setBugTrackingSystem(bugTrackingSystem));
		}
		return this;
	}

	@Override
	public BugTrackingSystem get() {
		return bugTrackingSystem;
	}
}
