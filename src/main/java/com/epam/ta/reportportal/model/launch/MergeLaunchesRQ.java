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

package com.epam.ta.reportportal.model.launch;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_PARAMETERS_LENGTH;

import com.epam.ta.reportportal.ws.annotations.NotBlankWithSize;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class MergeLaunchesRQ {

	@NotBlankWithSize(min = ValidationConstraints.MIN_LAUNCH_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
	@JsonProperty(value = "name", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String name;

	@JsonProperty(value = "description")
	private String description;

	@Size(max = MAX_PARAMETERS_LENGTH)
	@Valid
	@JsonProperty("attributes")
	private Set<ItemAttributeResource> attributes;

	@JsonProperty(value = "startTime")
	@Schema
	private LocalDateTime startTime;

	@JsonProperty("mode")
	private com.epam.ta.reportportal.ws.model.launch.Mode mode;

	@NotEmpty
	@JsonProperty(value = "launches", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private Set<Long> launches;

	@JsonProperty(value = "endTime")
	@Schema
	private LocalDateTime endTime;

	@NotNull
	@JsonProperty("mergeType")
	@Schema(allowableValues = "BASIC, DEEP")
	private String mergeStrategyType;

	@JsonProperty(value = "extendSuitesDescription", required = true)
	private boolean extendSuitesDescription;

}
