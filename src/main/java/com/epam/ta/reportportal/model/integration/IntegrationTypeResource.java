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

package com.epam.ta.reportportal.model.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Pavel Bortnik
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class IntegrationTypeResource implements Serializable {

	@JsonProperty("type")
	private Long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("authFlow")
	private AuthFlowEnum authFlow;

	@JsonProperty("creationDate")
	private Instant creationDate;

	@JsonProperty("groupType")
	private String groupType;

	@JsonProperty("details")
	private Map<String, Object> details;

	@JsonProperty("enabled")
	public boolean isEnabled() {
		return enabled;
	}

}
