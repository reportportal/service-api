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

package com.epam.ta.reportportal.store.database.entity.launch;

import com.epam.ta.reportportal.store.commons.querygen.FilterCriteria;
import com.epam.ta.reportportal.store.database.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.store.database.entity.enums.PostgreSQLEnumType;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.ExecutionStatistics;
import com.epam.ta.reportportal.store.database.entity.item.IssueStatistics;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "pqsql_enum", typeClass = PostgreSQLEnumType.class)
@Table(name = "launch", schema = "public", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "name", "number", "project_id" }) }, indexes = {
		@Index(name = "launch_pk", unique = true, columnList = "id ASC"),
		@Index(name = "unq_name_number", unique = true, columnList = "name ASC, number ASC, project_id ASC") })
public class Launch implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "uuid", unique = true, nullable = false)
	private String uuid;

	@Column(name = "project_id", nullable = false, precision = 32)
	private Long projectId;

	@Column(name = "user_id", nullable = false, precision = 32)
	private Long userId;

	@Column(name = "name", nullable = false, length = 256)
	@FilterCriteria("name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "start_time", nullable = false, updatable = false)
	private LocalDateTime startTime;

	@Column(name = "end_time", nullable = false, updatable = false)
	private LocalDateTime endTime;

	@Column(name = "number", nullable = false, precision = 32)
	private Long number;

	@Column(name = "last_modified", nullable = false)
	@LastModifiedDate
	private LocalDateTime lastModified;

	@Column(name = "mode", nullable = false)
	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	private LaunchModeEnum mode;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	private StatusEnum status;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "launch_id")
	private Set<LaunchTag> tags = Sets.newHashSet();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "launch_id", insertable = false, updatable = false)
	private Set<ExecutionStatistics> executionStatistics;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "launch_id", insertable = false, updatable = false)
	private Set<IssueStatistics> issueStatistics;

	public Set<LaunchTag> getTags() {
		return tags;
	}

	public void setTags(Set<LaunchTag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}

	public Launch(Long id, String uuid, Long projectId, Long userId, String name, String description, LocalDateTime startTime,
			LocalDateTime endTime, Long number, LocalDateTime lastModified, LaunchModeEnum mode, StatusEnum status) {
		this.id = id;
		this.uuid = uuid;
		this.projectId = projectId;
		this.userId = userId;
		this.name = name;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.number = number;
		this.lastModified = lastModified;
		this.mode = mode;
		this.status = status;
	}

	public Launch() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public LaunchModeEnum getMode() {
		return mode;
	}

	public void setMode(LaunchModeEnum mode) {
		this.mode = mode;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Launch{" + "id=" + id + ", projectId=" + projectId + ", userId=" + userId + ", name='" + name + '\'' + ", description='"
				+ description + '\'' + ", startTime=" + startTime + ", number=" + number + ", lastModified=" + lastModified + ", mode="
				+ mode + ", status=" + status + ", tags=" + tags + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Launch launch = (Launch) o;
		return Objects.equals(id, launch.id) && Objects.equals(projectId, launch.projectId) && Objects.equals(userId, launch.userId)
				&& Objects.equals(name, launch.name) && Objects.equals(description, launch.description) && Objects.equals(startTime,
				launch.startTime
		) && Objects.equals(number, launch.number) && Objects.equals(lastModified, launch.lastModified) && mode == launch.mode
				&& status == launch.status && Objects.equals(tags, launch.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, userId, name, description, startTime, number, lastModified, mode, status, tags);
	}
}
