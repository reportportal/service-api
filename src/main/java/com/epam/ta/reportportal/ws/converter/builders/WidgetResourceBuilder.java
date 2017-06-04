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

package com.epam.ta.reportportal.ws.converter.builders;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;

/**
 * Builder for {@link WidgetResource} object.
 * 
 * @author Aliaksei_Makayed
 */
@Service
@Scope("prototype")
public class WidgetResourceBuilder extends Builder<WidgetResource> {

	public WidgetResourceBuilder addWidget(Widget widget) {
		if (widget != null) {
			getObject().setWidgetId(widget.getId());
			getObject().setName(widget.getName());
			getObject().setDescription(widget.getDescription());
			if (null != widget.getApplyingFilterId()) {
				getObject().setApplyingFilterID(widget.getApplyingFilterId());
			}
			if (null != widget.getContentOptions()) {
				ContentParameters contentParameters = new ContentParameters();
				contentParameters.setType(widget.getContentOptions().getType());
				contentParameters.setGadget(widget.getContentOptions().getGadgetType());
				contentParameters.setMetadataFields(widget.getContentOptions().getMetadataFields());
				contentParameters.setContentFields(widget.getContentOptions().getContentFields());
				contentParameters.setItemsCount(widget.getContentOptions().getItemsCount());
				contentParameters.setWidgetOptions(widget.getContentOptions().getWidgetOptions());
				getObject().setContentParameters(contentParameters);
			}
			if (null != widget.getAcl()) {
				getObject().setOwner(widget.getAcl().getOwnerUserId());
				getObject().setShare(!widget.getAcl().getEntries().isEmpty());
			}
		}
		return this;
	}

	@Override
	protected WidgetResource initObject() {
		return new WidgetResource();
	}
}
