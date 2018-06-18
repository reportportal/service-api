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

import java.util.List;
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

	public WidgetBuilder addWidgetRq(WidgetRQ widgetRQ) {
		widget.setName(widgetRQ.getName());

		widget.getWidgetOptions().clear();
		widget.getWidgetOptions().addAll(widgetRQ.getContentParameters().getWidgetOptions().entrySet().stream().map(entry -> {
			WidgetOption option = new WidgetOption();
			option.setWidgetOption(entry.getKey());
			option.setValues(Sets.newHashSet(entry.getValue()));
			return option;
		}).collect(toSet()));

		widget.setWidgetType(widgetRQ.getContentParameters().getWidgetType());
		widget.setItemsCount(widgetRQ.getContentParameters().getItemsCount());

		widget.getContentFields().clear();
		widget.getContentFields().addAll(widgetRQ.getContentParameters().getContentFields());
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

	public WidgetBuilder addFilters(List<UserFilter> userFilters) {
		widget.getFilters().addAll(userFilters);
		userFilters.forEach(userFilter -> userFilter.getWidgets().add(widget));
		return this;
	}

	@Override
	public Widget get() {
		return widget;
	}
}
