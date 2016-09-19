/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;

/**
 * Implementation of {@link ExternalSystemResource} builder
 * 
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ExternalSystemResourceBuilder extends ResourceBuilder<ExternalSystemResource> {

	public ExternalSystemResourceBuilder addExternalSystem(ExternalSystem system) {
		ExternalSystemResource resource = getObject();
		resource.setSystemId(system.getId());
		resource.setUrl(system.getUrl());
		resource.setProjectRef(system.getProjectRef());
		resource.setProject(system.getProject());
		resource.setExternalSystemType(system.getExternalSystemType().name());
		resource.setExternalSystemAuth(system.getExternalSystemAuth().name());
		resource.setUsername(system.getUsername());
		resource.setAccessKey(system.getAccessKey());
		resource.setDomain(system.getDomain());
		resource.setFields(system.getFields());
		return this;
	}

	@Override
	protected ExternalSystemResource initObject() {
		return new ExternalSystemResource();
	}
}