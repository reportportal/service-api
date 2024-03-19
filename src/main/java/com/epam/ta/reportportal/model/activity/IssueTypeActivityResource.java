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

package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class IssueTypeActivityResource {

	@JsonProperty(value = "id", required = true)
	private Long id;

	@JsonProperty(value = "longName", required = true)
	private String longName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("IssueTypeActivityResource{");
		sb.append("id=").append(id);
		sb.append(", longName='").append(longName).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
