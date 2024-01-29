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

package com.epam.ta.reportportal.model.dashboard;

import com.epam.ta.reportportal.model.BaseEntityRQ;
import com.epam.ta.reportportal.ws.annotations.NotBlankWithSize;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;

/**
 * Domain object for updating widget positions.
 *
 * @author Pavel Bortnik
 */
@JsonInclude(Include.NON_NULL)
public class UpdateDashboardRQ extends BaseEntityRQ {

	@NotBlankWithSize(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_DASHBOARD_NAME_LENGTH)
	@JsonProperty(value = "name", required = true)
	@ApiModelProperty(required = true)
	private String name;

	@Valid
	@JsonProperty(value = "updateWidgets")
	private List<DashboardResource.WidgetObjectModel> widgets;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setWidgets(List<DashboardResource.WidgetObjectModel> value) {
		this.widgets = value;
	}

	public List<DashboardResource.WidgetObjectModel> getWidgets() {
		return widgets;
	}

	@Override
	public String toString() {
		return "UpdateDashboardRQ{" + "name='" + name + '\'' + ", widgets=" + widgets + '}';
	}
}