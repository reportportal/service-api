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

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.ta.reportportal.entity.bts.DefectFormField;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Pavel Bortnik
 */
public class BugTrackingSystemBuilder implements Supplier<Integration> {

	private Integration bugTrackingSystem;
	private IntegrationParams parameters;

	public BugTrackingSystemBuilder() {
		bugTrackingSystem = new Integration();
		parameters = new IntegrationParams(new HashMap<>());
	}

	public BugTrackingSystemBuilder(Integration bugTrackingSystem) {
		this.bugTrackingSystem = bugTrackingSystem;
		this.parameters = null != bugTrackingSystem.getParams() ? bugTrackingSystem.getParams() : new IntegrationParams(new HashMap<>());
	}

	public BugTrackingSystemBuilder addUrl(String url) {
		if (!StringUtils.isEmpty(url)) {
			if (url.endsWith("/")) {
				url = url.substring(0, url.length() - 1);
			}
		}
		BtsConstants.URL.setParam(parameters, url);
		return this;
	}

	public BugTrackingSystemBuilder addUsername(String username) {
		BtsConstants.USER_NAME.setParam(parameters, username);
		return this;
	}

	public BugTrackingSystemBuilder addPassword(String password) {
		BtsConstants.PASSWORD.setParam(parameters, password);
		return this;
	}

	public BugTrackingSystemBuilder addAuthType(String authType) {
		BtsConstants.AUTH_TYPE.setParam(parameters, authType);
		return this;
	}

	public BugTrackingSystemBuilder addIntegrationType(IntegrationType type) {
		this.bugTrackingSystem.setType(type);
		return this;
	}

	public BugTrackingSystemBuilder addBugTrackingProject(String project) {
		if (!StringUtils.isEmpty(project)) {
			BtsConstants.PROJECT.setParam(parameters, project);

		}
		return this;
	}

	public BugTrackingSystemBuilder addProject(Project project) {
		bugTrackingSystem.setProject(project);
		return this;
	}

	public BugTrackingSystemBuilder addFields(Set<DefectFormField> fields) {
		if (!CollectionUtils.isEmpty(fields)) {
			BtsConstants.DEFECT_FORM_FIELDS.setParam(parameters, fields);
		}
		return this;
	}

	public BugTrackingSystemBuilder addAuthKey(String authType) {
		BtsConstants.OAUTH_ACCESS_KEY.setParam(parameters, authType);
		return this;
	}

	@Override
	public Integration get() {
		this.bugTrackingSystem.setParams(parameters);
		return bugTrackingSystem;
	}
}
