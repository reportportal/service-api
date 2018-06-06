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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.store.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.store.database.entity.widget.WidgetOption;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Sets;

import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
public class WidgetBuilder implements Supplier<Widget> {

	private Widget widget;

	public WidgetBuilder() {
		widget = new Widget();
	}

	public WidgetBuilder(Widget widget) {
		this.widget = widget;
	}

	public WidgetBuilder addWidgetRq(WidgetRQ createWidget) {
		widget.setName(createWidget.getName());
		widget.setWidgetOptions(createWidget.getContentParameters().getWidgetOptions().entrySet().stream().map(entry -> {
			WidgetOption option = new WidgetOption();
			option.setWidgetOption(entry.getKey());
			option.setValues(Sets.newHashSet(entry.getValue()));
			return option;
		}).collect(toSet()));

		widget.setWidgetType(createWidget.getContentParameters().getType());
		widget.setItemsCount(createWidget.getContentParameters().getItemsCount());
		widget.setContentFields(createWidget.getContentParameters().getContentFields());
		return this;
	}

	public WidgetBuilder addProject(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		widget.setProject(project);
		return this;
	}

	public WidgetBuilder addFilter(UserFilter userFilter) {
		widget.getFilters().add(userFilter);
		userFilter.getWidgets().add(widget);
		return this;
	}

	@Override
	public Widget get() {
		return widget;
	}
}
