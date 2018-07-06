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
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@TypeDef(name = "pqsql_enum", typeClass = PostgreSQLEnumType.class)
@Table(name = "test_item_results", schema = "public")
public class TestItemResults implements Serializable {

	@Id
	@Column(name = "result_id", unique = true, nullable = false, precision = 64)
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
	@MapsId
	@JoinColumn(name = "result_id")
	private IssueEntity issue;

	@OneToOne(cascade = CascadeType.ALL)
	@MapsId
	@JoinColumn(name = "result_id")
	private TestItemStructure itemStructure;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", insertable = false, updatable = false)
	private Set<ExecutionStatistics> executionStatistics;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", insertable = false, updatable = false)
	private Set<IssueStatistics> issueStatistics;

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

	public Set<ExecutionStatistics> getExecutionStatistics() {
		return executionStatistics;
	}

	public void setExecutionStatistics(Set<ExecutionStatistics> executionStatistics) {
		this.executionStatistics = executionStatistics;
	}

	public Set<IssueStatistics> getIssueStatistics() {
		return issueStatistics;
	}

	public void setIssueStatistics(Set<IssueStatistics> issueStatistics) {
		this.issueStatistics = issueStatistics;
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

	public IssueEntity getIssue() {
		return issue;
	}

	public void setIssue(IssueEntity issue) {
		issue.setIssueId(this.itemId);
		this.issue = issue;
	}

	public TestItemStructure getItemStructure() {
		return itemStructure;
	}

	public void setItemStructure(TestItemStructure itemStructure) {
		this.itemStructure = itemStructure;
	}
}
