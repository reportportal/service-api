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

package com.epam.ta.reportportal.model.project.config;

import com.epam.ta.reportportal.ws.annotations.NotNullMapValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Project configuration model
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(Include.NON_NULL)
public class ProjectConfigurationUpdate {

	@NotNull
	@NotNullMapValue
	@JsonProperty(value = "attributes", required = true)
	private Map<String, String> projectAttributes;

	public Map<String, String> getProjectAttributes() {
		return projectAttributes;
	}

	public void setProjectAttributes(Map<String, String> projectAttributes) {
		this.projectAttributes = projectAttributes;
	}

}
