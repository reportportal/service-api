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

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class NestedStepResource implements Serializable {

	@JsonProperty(value = "id")
	private Long id;

	@JsonProperty(value = "name")
	private String name;

	@JsonProperty(value = "uuid")
	private String uuid;

	@JsonProperty(value = "type")
	private String type;

	@JsonProperty(value = "startTime")
	private Instant startTime;

	@JsonProperty(value = "endTime")
	private Instant endTime;

	@JsonProperty(value = "status")
	private String status;

	@JsonProperty(value = "duration")
	private Double duration;

	@JsonProperty(value = "hasContent")
	private Boolean hasContent;

	@JsonProperty(value = "attachmentsCount")
	private Integer attachmentsCount;

}
