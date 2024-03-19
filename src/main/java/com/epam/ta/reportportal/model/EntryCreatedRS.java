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
 * Response when some entry has been created and we need to return ID
 * 
 * @author Andrei Varabyeu
 * 
 */
@JsonInclude(Include.NON_NULL)
public class EntryCreatedRS {

	@JsonProperty("id")
	private Long id;

	public EntryCreatedRS() {

	}

	public EntryCreatedRS(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EntryCreatedRS{");
		sb.append("id='").append(id).append('\'');
		sb.append('}');
		return sb.toString();
	}

}