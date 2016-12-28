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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Builder for {@link Widget}
 *
 * @author Aliaksei_Makayed
 *
 */
@Service
@Scope("prototype")
public class WidgetBuilder extends ShareableEntityBuilder<Widget> {

	public WidgetBuilder addWidgetRQ(WidgetRQ createRQ) {
		if (createRQ != null) {
			getObject().setDescription(createRQ.getDescription());
			if (createRQ.getName() != null) {
				getObject().setName(createRQ.getName().trim());
			}
			addContentParameters(createRQ.getContentParameters());
		}
		return this;
	}

	public WidgetBuilder addContentParameters(ContentParameters parameters) {
		if (parameters != null) {
			ContentOptions contentOptions = new ContentOptions();
			contentOptions.setType(parameters.getType());
			contentOptions.setGadgetType(parameters.getGadget());
			contentOptions.setContentFields(null != parameters.getContentFields()
					? Lists.newArrayList(EntityUtils.trimStrings(parameters.getContentFields())) : null);
			contentOptions.setMetadataFields(null != parameters.getMetadataFields()
					? Lists.newArrayList(EntityUtils.trimStrings(parameters.getMetadataFields())) : null);
			contentOptions.setItemsCount(parameters.getItemsCount());
			contentOptions.setWidgetOptions(null != parameters.getWidgetOptions() ? Maps.newHashMap(parameters.getWidgetOptions()) : null);
			getObject().setContentOptions(contentOptions);
		}
		return this;
	}

	public WidgetBuilder addProject(String projectName) {
		getObject().setProjectName(projectName);
		return this;
	}

	public WidgetBuilder addFilter(String applyingFilterId) {
		getObject().setApplyingFilterId(applyingFilterId);
		return this;
	}

	@Override
	protected Widget initObject() {
		return new Widget();
	}

	@Override
	public WidgetBuilder addSharing(String owner, String project, boolean isShare) {
		super.addAcl(owner, project, isShare);
		return this;
	}

}