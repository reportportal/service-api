/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ServerSettings;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.SettingsController;
import com.epam.ta.reportportal.ws.converter.builders.ServerSettingsResourceBuilder;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;

/**
 * REST Maturity Lvl3 rel object creation for response
 * 
 * @author Andrei_Ramanchuk
 *
 */
@Service
public class ServerSettingsResourceAssembler extends ResourceAssemblerSupport<ServerSettings, ServerSettingsResource> {

	@Autowired
	@Qualifier("serverSettingsResourceBuilder.reference")
	private LazyReference<ServerSettingsResourceBuilder> builder;

	public ServerSettingsResourceAssembler() {
		super(SettingsController.class, ServerSettingsResource.class);
	}

	@Override
	public ServerSettingsResource toResource(ServerSettings settings) {
		Link selfLink = ControllerLinkBuilder.linkTo(SettingsController.class).slash(settings).withSelfRel();
		ServerSettingsResourceBuilder resourceBuilder = builder.get();
		resourceBuilder.addServerSettings(settings).addLink(selfLink);
		return resourceBuilder.build();
	}
}