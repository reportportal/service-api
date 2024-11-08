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

package com.epam.ta.reportportal.model.project.config.pattern;

import static com.epam.reportportal.model.ValidationConstraints.MAX_ANALYSIS_PATTERN_NAME_LENGTH;
import static com.epam.reportportal.model.ValidationConstraints.MIN_ANALYSIS_PATTERN_NAME_LENGTH;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePatternTemplateRQ {

	@NotBlank
	@Size(min = MIN_ANALYSIS_PATTERN_NAME_LENGTH, max = MAX_ANALYSIS_PATTERN_NAME_LENGTH)
	@JsonProperty(value = "name")
	private String name;

	@NotBlank
	@JsonProperty(value = "value")
	private String value;

	@NotBlank
	@JsonProperty(value = "type")
	private String type;

	@NotNull
	@JsonProperty(value = "enabled")
	private Boolean enabled;

	public CreatePatternTemplateRQ() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "UpdatePatternTemplateRQ{" + "name='" + name + '\'' + ", value='" + value + '\'' + ", type='" + type + '\'' + ", enabled=" + enabled + '}';
	}
}
