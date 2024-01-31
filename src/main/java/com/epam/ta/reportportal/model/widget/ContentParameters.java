/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model.widget;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_WIDGET_LIMIT;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_WIDGET_LIMIT;

/**
 * Part of widget domain object. Describe chart parameters
 *
 * @author Aliaksei_Makayed
 */
@JsonInclude(Include.NON_NULL)
public class ContentParameters {

	// fields for main data(for example: graphs at the chart)
	@JsonProperty(value = "contentFields", required = true)
	private List<String> contentFields;

	@NotNull
	@JsonProperty(value = "itemsCount", required = true)
	private int itemsCount;

	@JsonProperty(value = "widgetOptions")
	private Map<String, Object> widgetOptions;

	public List<String> getContentFields() {
		return contentFields;
	}

	public void setContentFields(List<String> contentFields) {
		this.contentFields = contentFields;
	}

	public int getItemsCount() {
		return itemsCount;
	}

	public void setItemsCount(int itemsCount) {
		this.itemsCount = itemsCount;
	}

	public Map<String, Object> getWidgetOptions() {
		return widgetOptions;
	}

	public void setWidgetOptions(Map<String, Object> widgetOptions) {
		this.widgetOptions = widgetOptions;
	}
}