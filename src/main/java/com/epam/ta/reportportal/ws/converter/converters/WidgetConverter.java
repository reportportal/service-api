/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.Position;
import com.epam.ta.reportportal.ws.model.Size;
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class WidgetConverter {

	public static final Function<DashboardWidget, DashboardResource.WidgetObjectModel> TO_OBJECT_MODEL = dashboardWidget -> {
		DashboardResource.WidgetObjectModel objectModel = new DashboardResource.WidgetObjectModel();
		objectModel.setWidgetId(dashboardWidget.getId().getWidgetId());
		objectModel.setWidgetPosition(new Position(dashboardWidget.getPositionX(), dashboardWidget.getPositionY()));
		objectModel.setWidgetSize(new Size(dashboardWidget.getWidth(), dashboardWidget.getHeight()));
		return objectModel;
	};

	public static final Function<Widget, WidgetResource> TO_WIDGET_RESOURCE = widget -> {
		WidgetResource widgetResource = new WidgetResource();
		widgetResource.setWidgetId(widget.getId());
		widgetResource.setName(widget.getName());
		widgetResource.setWidgetType(widget.getWidgetType());
		widgetResource.setDescription(widget.getDescription());
		widgetResource.setShare(widget.isShared());
		widgetResource.setOwner(widget.getOwner());
		ofNullable(widget.getFilters()).ifPresent(filter -> widgetResource.setAppliedFilters(UserFilterConverter.FILTER_SET_TO_FILTER_RESOURCE
				.apply(filter)));
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setItemsCount(widget.getItemsCount());
		ofNullable(widget.getWidgetOptions()).ifPresent(wo -> contentParameters.setWidgetOptions(wo.getOptions()));
		contentParameters.setContentFields(Lists.newArrayList(widget.getContentFields()));
		widgetResource.setContentParameters(contentParameters);
		return widgetResource;
	};

	public static final Function<Widget, WidgetActivityResource> TO_ACTIVITY_RESOURCE = widget -> {
		WidgetActivityResource resource = new WidgetActivityResource();
		resource.setId(widget.getId());
		resource.setProjectId(widget.getProject().getId());
		resource.setName(widget.getName());
		resource.setDescription(widget.getDescription());
		resource.setItemsCount(widget.getItemsCount());
		resource.setContentFields(Sets.newHashSet(widget.getContentFields()));
		ofNullable(widget.getWidgetOptions()).ifPresent(wo -> resource.setWidgetOptions(wo.getOptions()));
		return resource;
	};

	/**
	 * Creates many-to-many object representation of dashboards and widgets
	 *
	 * @param model     Widget model object
	 * @param dashboard Dashboard
	 * @param widget    Widget
	 * @return many-to-many object representation
	 */
	public static DashboardWidget toDashboardWidget(DashboardResource.WidgetObjectModel model, Dashboard dashboard, Widget widget) {

		DashboardWidgetId id = new DashboardWidgetId();
		id.setDashboardId(dashboard.getId());
		id.setWidgetId(model.getWidgetId());

		DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setId(id);
		dashboardWidget.setWidgetName(widget.getName());
		dashboardWidget.setPositionX(model.getWidgetPosition().getX());
		dashboardWidget.setPositionY(model.getWidgetPosition().getY());
		dashboardWidget.setWidth(model.getWidgetSize().getWidth());
		dashboardWidget.setHeight(model.getWidgetSize().getHeight());
		dashboardWidget.setDashboard(dashboard);
		dashboardWidget.setWidget(widget);

		return dashboardWidget;
	}

}
