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

package com.epam.ta.reportportal.model.item;

import static com.epam.reportportal.model.ValidationConstraints.MAX_PARAMETERS_LENGTH;

import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * @author Dzmitry_Kavalets
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateTestItemRQ {

	@Size(max = MAX_PARAMETERS_LENGTH)
	@Valid
	@JsonProperty(value = "attributes")
	private Set<ItemAttributeResource> attributes;

	@JsonProperty(value = "description")
	private String description;

	@JsonProperty(value = "status")
	private String status;

	public Set<ItemAttributeResource> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<ItemAttributeResource> attributes) {
		this.attributes = attributes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
