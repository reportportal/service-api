/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents detailed index information in indexing operation response.
 *
 * @author Ivan Sharamet
 */
public class IndexRsIndex {

	public static final int STATUS_UPDATED = 200;
	public static final int STATUS_CREATED = 201;

	@JsonProperty("_index")
	private String index;

	@JsonProperty("_type")
	private String type;

	@JsonProperty("_id")
	private String id;

	@JsonProperty("_version")
	private int version;

	@JsonProperty("result")
	private String result;

	@JsonProperty("created")
	private boolean created;

	@JsonProperty("status")
	private int status;

	public IndexRsIndex() {
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean failed() {
		return status != STATUS_CREATED && status != STATUS_UPDATED;
	}
}
