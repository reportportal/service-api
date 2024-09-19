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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic representation for chart object with info for UI
 * 
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class ChartObject {

	@JsonProperty(value = "values")
	private Map<String, String> values;

	@JsonProperty(value = "name")
	private String name;

	@JsonProperty(value = "startTime")
	private String startTime;

	@JsonProperty(value = "number")
	private String number;

	@JsonProperty(value = "id")
	private String id;

	public ChartObject() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getName() {
		return name;
	}

	public void setStartTime(String value) {
		this.startTime = value;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setNumber(String value) {
		this.number = value;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ChartObject{");
		sb.append("values='").append(values).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", startTime='").append(startTime).append('\'');
		sb.append(", number='").append(number).append('\'');
		sb.append(", id='").append(id).append('\'');
		sb.append('}');
		return sb.toString();
	}
}