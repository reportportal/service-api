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

import java.util.List;

/**
 * Represents indexing operation response.
 *
 * @author Ivan Sharamet
 */
public class IndexRs {

	@JsonProperty("took")
	private int took;

	@JsonProperty("errors")
	private boolean errors;

	@JsonProperty("items")
	private List<IndexRsItem> items;

	public IndexRs() {
	}

	public int getTook() {
		return took;
	}

	public void setTook(int took) {
		this.took = took;
	}

	public boolean isErrors() {
		return errors;
	}

	public void setErrors(boolean errors) {
		this.errors = errors;
	}

	public List<IndexRsItem> getItems() {
		return items;
	}

	public void setItems(List<IndexRsItem> items) {
		this.items = items;
	}
}
