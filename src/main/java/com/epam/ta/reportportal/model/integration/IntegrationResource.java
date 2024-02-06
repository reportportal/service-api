/*
 * Copyright 2021 EPAM Systems
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
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Pavel Bortnik
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class IntegrationResource implements Serializable {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("projectId")
	private Long projectId;

	@JsonProperty("name")
	private String name;

	@JsonProperty("integrationType")
	private IntegrationTypeResource integrationType;

	@JsonProperty("integrationParameters")
	private Map<String, Object> integrationParams;

	@JsonProperty("enabled")
	private Boolean enabled;

	@JsonProperty("creator")
	private String creator;

	@JsonProperty("creationDate")
	private Date creationDate;

}
