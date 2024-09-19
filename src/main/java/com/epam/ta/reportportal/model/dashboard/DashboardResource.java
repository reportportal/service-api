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

import com.epam.ta.reportportal.model.Position;
import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Domain model DashBoard resource object. JSON Representation of Report Portal
 * domain object.
 *
 * @author Aliaksei_Makayed
 */
@JsonInclude(Include.NON_NULL)
public class DashboardResource extends OwnedResource {

	@NotNull
	@JsonProperty(value = "id", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private Long dashboardId;

	@NotBlank
	@Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_DASHBOARD_NAME_LENGTH)
	@JsonProperty(value = "name", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String name;

	@JsonProperty(value = "widgets")
	private List<WidgetObjectModel> widgets;

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WidgetObjectModel> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<WidgetObjectModel> widgets) {
		this.widgets = widgets;
	}

	@JsonInclude(Include.NON_NULL)
	public static class WidgetObjectModel {

		@JsonProperty(value = "widgetName")
		private String name;

		@NotNull
		@JsonProperty(value = "widgetId")
		private Long widgetId;

		@JsonProperty(value = "widgetType")
		private String widgetType;

		@JsonProperty(value = "widgetSize")
		private com.epam.ta.reportportal.model.Size widgetSize = new com.epam.ta.reportportal.model.Size();

		@JsonProperty(value = "widgetPosition")
		private Position widgetPosition = new Position();

		@JsonProperty(value = "widgetOptions")
		private Map<String, Object> widgetOptions;

		public WidgetObjectModel() {
		}

		public WidgetObjectModel(String name, Long widgetId, com.epam.ta.reportportal.model.Size widgetSize, Position widgetPosition) {
			this.name = name;
			this.widgetId = widgetId;
			this.widgetSize = widgetSize;
			this.widgetPosition = widgetPosition;
		}

		public Long getWidgetId() {
			return widgetId;
		}

		public void setWidgetId(Long widgetId) {
			this.widgetId = widgetId;
		}

		public String getWidgetType() {
			return widgetType;
		}

		public void setWidgetType(String widgetType) {
			this.widgetType = widgetType;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public com.epam.ta.reportportal.model.Size getWidgetSize() {
			return widgetSize;
		}

		public void setWidgetSize(com.epam.ta.reportportal.model.Size widgetSize) {
			this.widgetSize = widgetSize;
		}

		public Position getWidgetPosition() {
			return widgetPosition;
		}

		public void setWidgetPosition(Position widgetPosition) {
			this.widgetPosition = widgetPosition;
		}

		public Map<String, Object> getWidgetOptions() {
			return widgetOptions;
		}

		public void setWidgetOptions(Map<String, Object> widgetOptions) {
			this.widgetOptions = widgetOptions;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("WidgetObjectModel{");
			sb.append("name='").append(name).append('\'');
			sb.append(", widgetId=").append(widgetId);
			sb.append(", widgetType='").append(widgetType).append('\'');
			sb.append(", widgetSize=").append(widgetSize);
			sb.append(", widgetPosition=").append(widgetPosition);
			sb.append(", widgetOptions=").append(widgetOptions);
			sb.append('}');
			return sb.toString();
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DashboardResource{");
		sb.append("dashboardId='").append(dashboardId).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", widgets=").append(widgets);
		sb.append('}');
		return sb.toString();
	}
}
