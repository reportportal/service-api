/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.model.launch.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateClustersRQ {

	@NotNull
	@JsonProperty(value = "launchId", required = true)
	private Long launchId;

	@JsonProperty(value = "removeNumbers")
	private boolean removeNumbers;

	public CreateClustersRQ() {
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public boolean isRemoveNumbers() {
		return removeNumbers;
	}

	public void setRemoveNumbers(boolean removeNumbers) {
		this.removeNumbers = removeNumbers;
	}
}
