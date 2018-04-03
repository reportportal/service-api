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
import com.epam.ta.reportportal.store.database.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
@Table(name = "test_item", schema = "public", indexes = { @Index(name = "test_item_pk", unique = true, columnList = "item_id ASC") })
public class TestItem implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id", unique = true, nullable = false, precision = 64)
	private Long itemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "launch_id")
	private Launch launch;

	@Column(name = "name", length = 256)
	private String name;

	@Enumerated(EnumType.STRING)
	@Type(type = "pqsql_enum")
	@Column(name = "type", nullable = false)
	private TestItemTypeEnum type;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "description")
	private String description;

	@LastModifiedDate
	@Column(name = "last_modified", nullable = false)
	private LocalDateTime lastModified;

	@ElementCollection
	@CollectionTable(name = "parameter", joinColumns = @JoinColumn(name = "item_id"))
	private List<Parameter> parameters;

	@Column(name = "unique_id", nullable = false, length = 256)
	private String uniqueId;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "item_id")
	private Set<TestItemTag> tags = Sets.newHashSet();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "item_id")
	private Set<Log> logs = Sets.newHashSet();

	@OneToOne(mappedBy = "testItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private TestItemStructure testItemStructure;

	@OneToOne(mappedBy = "testItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private TestItemResults testItemResults;

	public TestItem() {
	}

	public TestItemStructure getTestItemStructure() {
		return testItemStructure;
	}

	public void setTestItemStructure(TestItemStructure testItemStructure) {
		this.testItemStructure = testItemStructure;
		testItemStructure.setTestItem(this);
	}

	public TestItemResults getTestItemResults() {
		return testItemResults;
	}

	public void setTestItemResults(TestItemResults testItemResults) {
		this.testItemResults = testItemResults;
		testItemResults.setTestItem(this);
	}

	public Set<TestItemTag> getTags() {
		return tags;
	}

	public void setTags(Set<TestItemTag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}

	public Set<Log> getLogs() {
		return logs;
	}

	public void setLogs(Set<Log> logs) {
		this.logs.clear();
		this.logs.addAll(logs);
	}

	public void addLog(Log log) {
		logs.add(log);
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Launch getLaunch() {
		return launch;
	}

	public void setLaunch(Launch launch) {
		this.launch = launch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestItemTypeEnum getType() {
		return type;
	}

	public void setType(TestItemTypeEnum type) {
		this.type = type;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		return "TestItem{" + "itemId=" + itemId + ", name='" + name + '\'' + ", type=" + type + ", startTime=" + startTime
				+ ", description='" + description + '\'' + ", lastModified=" + lastModified + ", uniqueId='" + uniqueId + '\'' + ", tags="
				+ tags + ", testItemStructure=" + testItemStructure + ", testItemResults=" + testItemResults + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestItem testItem = (TestItem) o;
		return Objects.equals(itemId, testItem.itemId) && Objects.equals(name, testItem.name) && type == testItem.type && Objects.equals(
				startTime, testItem.startTime) && Objects.equals(description, testItem.description) && Objects.equals(
				lastModified, testItem.lastModified) && Objects.equals(uniqueId, testItem.uniqueId) && Objects.equals(tags, testItem.tags)
				&& Objects.equals(
				testItemStructure, testItem.testItemStructure) && Objects.equals(testItemResults, testItem.testItemResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId, name, type, startTime, description, lastModified, uniqueId, tags, testItemStructure, testItemResults);
	}
}
