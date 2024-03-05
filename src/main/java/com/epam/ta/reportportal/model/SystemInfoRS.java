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

/**
 * System information response
 * 
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class SystemInfoRS {

	@JsonProperty("os")
	private String osVersion;

	@JsonProperty("cpuUsage")
	private float cpuUsage;

	@JsonProperty("memUsage")
	private float memUsage;

	public void setOsVersion(String value) {
		this.osVersion = value;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setCpuUsage(float value) {
		this.cpuUsage = value;
	}

	public float getCpuUsage() {
		return cpuUsage;
	}

	public void setMemUsage(float value) {
		this.memUsage = value;
	}

	public float getMemUsage() {
		return memUsage;
	}
}