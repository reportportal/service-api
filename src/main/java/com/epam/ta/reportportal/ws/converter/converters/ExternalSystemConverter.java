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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.auth.ApiKeyAuth;
import com.epam.ta.reportportal.store.database.entity.bts.auth.BasicAuth;
import com.epam.ta.reportportal.store.database.entity.bts.auth.NtlmAuth;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.function.Function;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Pavel Bortnik
 */
public final class ExternalSystemConverter {

	private ExternalSystemConverter() {
		//static only
	}

	public static final Function<BugTrackingSystem, ExternalSystemResource> TO_RESOURCE = bugTrackingSystem -> {
		Preconditions.checkNotNull(bugTrackingSystem);
		ExternalSystemResource resource = new ExternalSystemResource();
		resource.setSystemId(bugTrackingSystem.getId().toString());
		resource.setUrl(bugTrackingSystem.getUrl());
		resource.setProjectRef(bugTrackingSystem.getProjectId().toString());
		resource.setProject(bugTrackingSystem.getBtsProject());
		resource.setExternalSystemType(bugTrackingSystem.getBtsType());
		resource.setExternalSystemAuth(bugTrackingSystem.getAuth().getAuthType().name());

		if (bugTrackingSystem.getAuth() instanceof BasicAuth) {
			resource.setUsername(((BasicAuth) bugTrackingSystem.getAuth()).getUsername());
		} else if (bugTrackingSystem.getAuth() instanceof ApiKeyAuth) {
			resource.setAccessKey(((ApiKeyAuth) bugTrackingSystem.getAuth()).getAccessKey());
		} else if (bugTrackingSystem.getAuth() instanceof NtlmAuth) {
			NtlmAuth auth = (NtlmAuth) bugTrackingSystem.getAuth();
			resource.setUsername(auth.getUsername());
			resource.setDomain(auth.getDomain());
		}
		resource.setFields(Collections.emptyList());
		return resource;
	};
}
