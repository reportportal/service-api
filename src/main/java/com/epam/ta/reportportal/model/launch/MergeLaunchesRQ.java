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
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Date;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(Include.NON_NULL)
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
	private Date startTime;

	@JsonProperty("mode")
	private com.epam.ta.reportportal.ws.model.launch.Mode mode;

	@NotEmpty
	@JsonProperty(value = "launches", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private Set<Long> launches;

	@JsonProperty(value = "endTime")
	@Schema
	private Date endTime;

	@NotNull
	@JsonProperty("mergeType")
	@Schema(allowableValues = "BASIC, DEEP")
	private String mergeStrategyType;

	@JsonProperty(value = "extendSuitesDescription", required = true)
	private boolean extendSuitesDescription;

	public String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public com.epam.ta.reportportal.ws.model.launch.Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@NotNull
	public Set<Long> getLaunches() {
		return launches;
	}

	public void setLaunches(@NotNull Set<Long> launches) {
		this.launches = launches;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@NotNull
	public String getMergeStrategyType() {
		return mergeStrategyType;
	}

	public void setMergeStrategyType(@NotNull String mergeStrategyType) {
		this.mergeStrategyType = mergeStrategyType;
	}

	public boolean isExtendSuitesDescription() {
		return extendSuitesDescription;
	}

	public void setExtendSuitesDescription(boolean extendSuitesDescription) {
		this.extendSuitesDescription = extendSuitesDescription;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MergeLaunchesRQ that = (MergeLaunchesRQ) o;

		if (extendSuitesDescription != that.extendSuitesDescription) {
			return false;
		}
		if (!name.equals(that.name)) {
			return false;
		}
		if (description != null ? !description.equals(that.description) : that.description != null) {
			return false;
		}
		if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) {
			return false;
		}
		if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
			return false;
		}
		if (mode != that.mode) {
			return false;
		}
		if (!launches.equals(that.launches)) {
			return false;
		}
		if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) {
			return false;
		}
		return mergeStrategyType.equals(that.mergeStrategyType);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
		result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
		result = 31 * result + (mode != null ? mode.hashCode() : 0);
		result = 31 * result + launches.hashCode();
		result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
		result = 31 * result + mergeStrategyType.hashCode();
		result = 31 * result + (extendSuitesDescription ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "MergeLaunchesRQ{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", attributes=" + attributes
				+ ", startTime=" + startTime + ", mode=" + mode + ", launches=" + launches + ", endTime=" + endTime
				+ ", mergeStrategyType='" + mergeStrategyType + '\'' + ", extendSuitesDescription=" + extendSuitesDescription + '}';
	}
}