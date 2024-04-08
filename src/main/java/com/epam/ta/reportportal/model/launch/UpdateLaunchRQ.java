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

import static com.epam.reportportal.model.ValidationConstraints.MAX_PARAMETERS_LENGTH;

import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Size;

/**
 * Domain object for updating launch object.
 *
 * @author Aliaksei_Makayed
 */
@JsonInclude(Include.NON_NULL)
public class UpdateLaunchRQ {

	@JsonProperty("mode")
	@Schema(allowableValues = "DEFAULT, DEBUG")
	private Mode mode;

	@JsonProperty("description")
	private String description;

	@Size(max = MAX_PARAMETERS_LENGTH)
	@Valid
	@JsonProperty("attributes")
	private Set<ItemAttributeResource> attributes;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<ItemAttributeResource> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<ItemAttributeResource> attributes) {
		this.attributes = attributes;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("UpdateLaunchRQ{");
		sb.append("mode=").append(mode);
		sb.append('}');
		return sb.toString();
	}
}