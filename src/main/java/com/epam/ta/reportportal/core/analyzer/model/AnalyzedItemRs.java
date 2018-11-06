/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class AnalyzedItemRs {

	@JsonProperty("test_item")
	private Long itemId;

	@JsonProperty("relevant_item")
	private Long relevantItemId;

	@JsonProperty("issue_type")
	private Long issueTypeId;

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

	public Long getIssueTypeId() {
		return issueTypeId;
	}

	public void setIssueTypeId(Long issueTypeId) {
		this.issueTypeId = issueTypeId;
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
}
