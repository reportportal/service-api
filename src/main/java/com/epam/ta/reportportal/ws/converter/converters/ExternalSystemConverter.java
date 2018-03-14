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

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.google.common.base.Preconditions;

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

	public static final Function<ExternalSystem, ExternalSystemResource> TO_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		ExternalSystemResource resource = new ExternalSystemResource();
		resource.setSystemId(model.getId());
		resource.setUrl(model.getUrl());
		resource.setProjectRef(model.getProjectRef());
		resource.setProject(model.getProject());
		resource.setExternalSystemType(model.getExternalSystemType());
		resource.setExternalSystemAuth(model.getExternalSystemAuth().name());
		resource.setUsername(model.getUsername());
		resource.setAccessKey(model.getAccessKey());
		resource.setDomain(model.getDomain());
		resource.setFields(model.getFields());
		return resource;
	};
}
