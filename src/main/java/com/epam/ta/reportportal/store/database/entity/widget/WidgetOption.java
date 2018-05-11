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

package com.epam.ta.reportportal.store.database.entity.widget;

import com.epam.ta.reportportal.store.database.entity.JsonbObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
@JsonTypeName("widgetOption")
public class WidgetOption extends JsonbObject {

	private Map<String, List<String>> options;

	public WidgetOption() {
	}

	@JsonCreator
	public WidgetOption(@JsonProperty("options") Map<String, List<String>> options) {
		this.options = options;
	}

	public Map<String, List<String>> getOptions() {
		return options;
	}

	public void setOptions(Map<String, List<String>> options) {
		this.options = options;
	}
}
