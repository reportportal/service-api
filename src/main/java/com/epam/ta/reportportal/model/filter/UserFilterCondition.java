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

package com.epam.ta.reportportal.model.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * Filter entity domain model object.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class UserFilterCondition {

	@NotBlank
	@JsonProperty(value = "filteringField", required = true)
	private String filteringField;

	@NotBlank
	@JsonProperty(value = "condition", required = true)
	private String condition;

	@NotBlank
	@JsonProperty(value = "value", required = true)
	private String value;

	public UserFilterCondition() {
	}

	public UserFilterCondition(String field, String condition, String value) {
		this.filteringField = field;
		this.condition = condition;
		this.value = value;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFilteringField() {
		return filteringField;
	}

	public void setFilteringField(String filteringField) {
		this.filteringField = filteringField;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((filteringField == null) ? 0 : filteringField.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UserFilterCondition other = (UserFilterCondition) obj;
		if (condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!condition.equals(other.condition)) {
			return false;
		}
		if (filteringField == null) {
			if (other.filteringField != null) {
				return false;
			}
		} else if (!filteringField.equals(other.filteringField)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("UserFilterEntity{");
		sb.append("filteringField='").append(filteringField).append('\'');
		sb.append(", condition='").append(condition).append('\'');
		sb.append(", value='").append(value).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
