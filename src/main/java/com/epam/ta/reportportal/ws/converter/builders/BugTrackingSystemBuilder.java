/*
 * Copyright 2017 EPAM Systems
 *
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
 */

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuth;
import com.epam.ta.reportportal.store.database.entity.bts.DefectFormField;
import com.epam.ta.reportportal.store.database.entity.project.Project;
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

	public BugTrackingSystemBuilder addSystemAuth(BugTrackingSystemAuth auth) {
		bugTrackingSystem.setAuth(auth);
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
