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
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.stream.Collectors;

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
		resource.setProjectRef(bugTrackingSystem.getProject().getId().toString());
		resource.setProject(bugTrackingSystem.getBtsProject());
		resource.setExternalSystemType(bugTrackingSystem.getBtsType());
		resource.setExternalSystemAuth(bugTrackingSystem.getAuth().getAuthType().name());
		resource.setUsername(bugTrackingSystem.getAuth().getUsername());
		resource.setDomain(bugTrackingSystem.getAuth().getDomain());
		resource.setAccessKey(bugTrackingSystem.getAuth().getAccessKey());
		resource.setFields(bugTrackingSystem.getDefectFormFields()
				.stream()
				.map(ExternalSystemFieldsConverter.FIELD_TO_MODEL)
				.collect(Collectors.toList()));
		return resource;
	};
}
