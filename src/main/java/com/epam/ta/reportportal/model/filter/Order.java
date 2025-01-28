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

package com.epam.ta.reportportal.model.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

/**
 * @author Pavel Bortnik
 */
public class Order {

	@NotNull
	@JsonProperty(value = "sortingColumn", required = true)
	private String sortingColumnName;

	@NotNull
	@JsonProperty(value = "isAsc", required = true)
	private boolean isAsc;

	public String getSortingColumnName() {
		return sortingColumnName;
	}

	public void setSortingColumnName(String sortingColumnName) {
		this.sortingColumnName = sortingColumnName;
	}

	public boolean getIsAsc() {
		return isAsc;
	}

	public void setIsAsc(boolean isAsc) {
		this.isAsc = isAsc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Order order = (Order) o;

		if (isAsc != order.isAsc) {
			return false;
		}
		return sortingColumnName != null ? sortingColumnName.equals(order.sortingColumnName) : order.sortingColumnName == null;
	}

	@Override
	public int hashCode() {
		int result = sortingColumnName != null ? sortingColumnName.hashCode() : 0;
		result = 31 * result + (isAsc ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Order{" + "sortingColumnName='" + sortingColumnName + '\'' + ", isAsc=" + isAsc + '}';
	}
}
