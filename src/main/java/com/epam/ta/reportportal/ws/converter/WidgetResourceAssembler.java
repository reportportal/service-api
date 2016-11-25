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

import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.UserFilterController;
import com.epam.ta.reportportal.ws.controller.impl.WidgetController;
import com.epam.ta.reportportal.ws.converter.builders.WidgetResourceBuilder;

/**
 * Resource Assembler for the {@link Widget} DB entity.
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
public class WidgetResourceAssembler extends ResourceAssemblerSupport<Widget, WidgetResource> {

	public static final String REL = "related";

	@Autowired
	@Qualifier("widgetResourceBuilder.reference")
	private LazyReference<WidgetResourceBuilder> builderLazyReference;

	public WidgetResourceAssembler() {
		super(WidgetController.class, WidgetResource.class);
	}

	@Override
	public WidgetResource toResource(Widget widget) {
		Link selfLink = ControllerLinkBuilder.linkTo(WidgetController.class, widget.getProjectName()).slash(widget).withSelfRel();
		WidgetResourceBuilder widgetResourceBuilder = builderLazyReference.get();
		widgetResourceBuilder.addWidget(widget).addLink(selfLink);
		if (widget.getApplyingFilterId() != null) {
			Link filterLink = ControllerLinkBuilder.linkTo(UserFilterController.class, widget.getProjectName())
					.slash(widget.getApplyingFilterId()).withRel(REL);
			widgetResourceBuilder.addLink(filterLink);
		}
		return widgetResourceBuilder.build();
	}
}