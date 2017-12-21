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

import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.google.common.base.Preconditions;

import java.util.Optional;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class WidgetConverter {

	private WidgetConverter() {
		//static only
	}

	public static final Function<Widget, WidgetResource> TO_RESOURCE = widget -> {
		Preconditions.checkNotNull(widget);
		WidgetResource widgetResource = new WidgetResource();
		widgetResource.setWidgetId(widget.getId());
		widgetResource.setName(widget.getName());
		widgetResource.setDescription(widget.getDescription());
		widgetResource.setFilterId(widget.getApplyingFilterId());
		Optional.ofNullable(widget.getContentOptions()).ifPresent(options -> {
			ContentParameters contentParameters = new ContentParameters();
			contentParameters.setType(options.getType());
			contentParameters.setGadget(options.getGadgetType());
			contentParameters.setMetadataFields(options.getMetadataFields());
			contentParameters.setContentFields(options.getContentFields());
			contentParameters.setItemsCount(options.getItemsCount());
			contentParameters.setWidgetOptions(options.getWidgetOptions());
			widgetResource.setContentParameters(contentParameters);
		});
		Optional.ofNullable(widget.getAcl()).ifPresent(acl -> {
			widgetResource.setOwner(acl.getOwnerUserId());
			widgetResource.setShare(!acl.getEntries().isEmpty());
		});
		return widgetResource;
	};

}
