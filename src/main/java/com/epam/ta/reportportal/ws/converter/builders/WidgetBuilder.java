/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

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
		ofNullable(widgetRQ.getName()).ifPresent(name -> widget.setName(name));
		ofNullable(widgetRQ.getShare()).ifPresent(it -> widget.setShared(it));
		widget.setDescription(widgetRQ.getDescription());

		ofNullable(widgetRQ.getContentParameters().getWidgetOptions()).ifPresent(wo -> {
			WidgetOptions widgetOptions = ofNullable(widget.getWidgetOptions()).orElseGet(WidgetOptions::new);
			Map<String, Object> options = ofNullable(widgetOptions.getOptions()).orElseGet(LinkedHashMap::new);
			options.putAll(wo);
			widgetOptions.setOptions(options);
			widget.setWidgetOptions(widgetOptions);
		});

		widget.setWidgetType(widgetRQ.getWidgetType());
		widget.setItemsCount(widgetRQ.getContentParameters().getItemsCount());

		widget.getContentFields().clear();
		widget.getContentFields().addAll(ofNullable(widgetRQ.getContentParameters().getContentFields()).orElse(Collections.emptyList()));
		return this;
	}

	public WidgetBuilder addWidgetPreviewRq(WidgetPreviewRQ previewRQ) {
		WidgetOptions widgetOptions = ofNullable(widget.getWidgetOptions()).orElseGet(WidgetOptions::new);
		Map<String, Object> options = ofNullable(widgetOptions.getOptions()).orElseGet(LinkedHashMap::new);
		options.putAll(previewRQ.getContentParameters().getWidgetOptions());

		widgetOptions.setOptions(options);
		widget.setWidgetOptions(widgetOptions);

		widget.setWidgetType(previewRQ.getWidgetType());
		widget.setItemsCount(previewRQ.getContentParameters().getItemsCount());

		widget.getContentFields().clear();
		widget.getContentFields().addAll(ofNullable(previewRQ.getContentParameters().getContentFields()).orElse(Collections.emptyList()));
		return this;
	}

	public WidgetBuilder addProject(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		widget.setProject(project);
		return this;
	}

	public WidgetBuilder addFilters(Iterable<UserFilter> userFilters) {
		ofNullable(userFilters).ifPresent(it -> widget.setFilters(Sets.newLinkedHashSet(it)));
		return this;
	}

	public WidgetBuilder addOwner(String owner) {
		widget.setOwner(owner);
		return this;
	}

	public WidgetBuilder addOption(String key, Object value) {
		WidgetOptions widgetOptions = ofNullable(widget.getWidgetOptions()).orElseGet(() -> {
			WidgetOptions opts = new WidgetOptions();
			widget.setWidgetOptions(opts);
			return opts;
		});
		Map<String, Object> options = ofNullable(widgetOptions.getOptions()).orElseGet(() -> {
			LinkedHashMap<String, Object> opts = new LinkedHashMap<>();
			widgetOptions.setOptions(opts);
			return opts;
		});
		options.put(key, value);
		return this;
	}

	@Override
	public Widget get() {
		return widget;
	}
}
