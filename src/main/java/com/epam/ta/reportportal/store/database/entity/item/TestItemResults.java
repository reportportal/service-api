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

import com.epam.ta.reportportal.store.database.entity.enums.PostgreSQLEnumType;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
@Entity
@TypeDef(name = "pqsql_enum", typeClass = PostgreSQLEnumType.class)
@Table(name = "test_item_results", schema = "public", indexes = {
		@Index(name = "test_item_results_pk", unique = true, columnList = "item_id ASC") })
public class TestItemResults implements Serializable {

	@Id
	@Column(name = "item_id", unique = true, nullable = false, precision = 64)
	private Long itemId;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	private StatusEnum status;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Column(name = "duration")
	private Double duration;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private IssueEntity issue;

	@OneToOne
	@MapsId
	@JoinColumn(name = "item_id")
	private TestItem testItem;

	public TestItemResults() {
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public IssueEntity getIssue() {
		return issue;
	}

	public void setIssue(IssueEntity issue) {
		issue.setIssueId(this.itemId);
		this.issue = issue;
	}

	@Override
	public String toString() {
		return "TestItemResults{" + "itemId=" + itemId + ", status=" + status + ", duration=" + duration + ", issue=" + issue + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestItemResults that = (TestItemResults) o;
		return Objects.equals(itemId, that.itemId) && status == that.status && Objects.equals(duration, that.duration) && Objects.equals(
				issue, that.issue) && Objects.equals(testItem, that.testItem);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId, status, duration, issue, testItem);
	}
}
