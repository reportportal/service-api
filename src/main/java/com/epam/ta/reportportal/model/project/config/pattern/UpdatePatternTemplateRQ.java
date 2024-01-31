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

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_ANALYSIS_PATTERN_NAME_LENGTH;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_ANALYSIS_PATTERN_NAME_LENGTH;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatePatternTemplateRQ {

	@NotBlank
	@Size(min = MIN_ANALYSIS_PATTERN_NAME_LENGTH, max = MAX_ANALYSIS_PATTERN_NAME_LENGTH)
	@JsonProperty(value = "name")
	private String name;

	@NotNull
	@JsonProperty(value = "enabled")
	private Boolean enabled;

	public UpdatePatternTemplateRQ() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "UpdatePatternTemplateRQ{" + "name='" + name + '\'' + ", enabled=" + enabled + '}';
	}
}
