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

package com.epam.ta.reportportal.store.database.entity.item;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "test_item_structure", schema = "public")
public class TestItemStructure implements Serializable {

	@Id
	@Column(name = "structure_id", unique = true, nullable = false)
	private Long itemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private TestItemStructure parent;

	@OneToOne(mappedBy = "itemStructure")
	private TestItem testItem;

	@OneToOne(mappedBy = "itemStructure")
	private TestItemResults itemResults;

	@Column(name = "retry_of", precision = 64)
	private Long retryOf;

	public TestItemStructure() {
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public TestItemStructure getParent() {
		return parent;
	}

	public void setParent(TestItemStructure parent) {
		this.parent = parent;
	}

	public Long getRetryOf() {
		return retryOf;
	}

	public void setRetryOf(Long retryOf) {
		this.retryOf = retryOf;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public TestItemResults getItemResults() {
		return itemResults;
	}

	public void setItemResults(TestItemResults itemResults) {
		this.itemResults = itemResults;
	}
}
