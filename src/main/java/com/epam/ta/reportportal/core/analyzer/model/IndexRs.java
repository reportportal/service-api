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
