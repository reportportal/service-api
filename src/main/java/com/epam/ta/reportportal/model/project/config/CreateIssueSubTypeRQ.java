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

import static com.epam.reportportal.model.ValidationConstraints.HEX_COLOR_REGEXP;

import com.epam.reportportal.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request model for new issue sub type for specified project.<br>
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class CreateIssueSubTypeRQ {

	@NotBlank
	@JsonProperty(value = "typeRef", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String typeRef;

	@NotBlank
	@Size(min = ValidationConstraints.MIN_SUBTYPE_LONG_NAME, max = ValidationConstraints.MAX_SUBTYPE_LONG_NAME)
	@JsonProperty(value = "longName", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String longName;

	@NotBlank
	@Size(min = ValidationConstraints.MIN_SUBTYPE_SHORT_NAME, max = ValidationConstraints.MAX_SUBTYPE_SHORT_NAME)
	@JsonProperty(value = "shortName", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
	private String shortName;

	@NotBlank
	@Pattern(regexp = HEX_COLOR_REGEXP)
	@JsonProperty(value = "color", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
	private String color;

	public void setTypeRef(String typeRef) {
		this.typeRef = typeRef;
	}

	public String getTypeRef() {
		return typeRef;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getLongName() {
		return longName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}
}
