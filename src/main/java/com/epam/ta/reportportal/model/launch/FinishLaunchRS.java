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

import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model of launch start resource
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinishLaunchRS extends EntryCreatedAsyncRS {

	@JsonProperty("number")
	private Long number;

	@JsonProperty("link")
	private String link;

	public FinishLaunchRS() {
	}

	public FinishLaunchRS(String id, Long number, String link) {
		super(id);
		this.number = number;
		this.link = link;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FinishLaunchRS{");
		sb.append("number=").append(number);
		sb.append(", link='").append(link).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
