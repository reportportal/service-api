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

import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class AnalyzedItemRs {

	@JsonProperty("testItem")
	private Long itemId;

	@JsonProperty("relevantItem")
	private Long relevantItemId;

	@JsonProperty("issueType")
	private String locator;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getRelevantItemId() {
		return relevantItemId;
	}

	public void setRelevantItemId(Long relevantItemId) {
		this.relevantItemId = relevantItemId;
	}

	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AnalyzedItemRs that = (AnalyzedItemRs) o;
		return Objects.equals(itemId, that.itemId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId);
	}

	@Override
	public String toString() {
		return "AnalyzedItemRs{" + "itemId=" + itemId + ", relevantItemId=" + relevantItemId + ", issueTypeLocator=" + locator + '}';
	}
}
