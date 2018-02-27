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
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "test_item_structure", schema = "public", indexes = {
		@Index(name = "test_item_structure_pk", unique = true, columnList = "item_id ASC") })
public class TestItemStructure implements Serializable {

	@Id
	@Column(name = "item_id", unique = true, nullable = false)
	private Long itemId;

	@Column(name = "launch_id", nullable = false, precision = 64)
	private Long launchId;

	@Column(name = "parent_id", precision = 64)
	private Long parentId;

	@Column(name = "retry_of", precision = 64)
	private Long retryOf;

	@OneToOne
	@MapsId
	@JoinColumn(name = "item_id")
	private TestItem testItem;

	public TestItemStructure() {
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getRetryOf() {
		return retryOf;
	}

	public void setRetryOf(Long retryOf) {
		this.retryOf = retryOf;
	}

	@Override
	public String toString() {
		return "TestItemStructure{" + "itemId=" + itemId + ", launchId=" + launchId + ", parentId=" + parentId + ", retryOf=" + retryOf
				+ ", testItem=" + testItem + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestItemStructure that = (TestItemStructure) o;
		return Objects.equals(itemId, that.itemId) && Objects.equals(launchId, that.launchId) && Objects.equals(parentId, that.parentId)
				&& Objects.equals(retryOf, that.retryOf) && Objects.equals(testItem, that.testItem);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId, launchId, parentId, retryOf, testItem);
	}
}
