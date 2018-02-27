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

import com.epam.ta.reportportal.store.database.entity.enums.TestItemTypeEnum;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "test_item", schema = "public", indexes = { @Index(name = "test_item_pk", unique = true, columnList = "id ASC") })
public class TestItem implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "name", length = 256)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private TestItemTypeEnum type;

	@Column(name = "start_time", nullable = false)
	private Timestamp startTime;

	@Column(name = "description")
	private String description;

	@LastModifiedDate
	@Column(name = "last_modified", nullable = false)
	private Timestamp lastModified;

	@Column(name = "parameters")
	private Parameter[] parameters;

	@Column(name = "unique_id", nullable = false, length = 256)
	private String uniqueId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "item_id")
	private Set<TestItemTag> tags;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "item_id")
	private TestItemResults testItemResults;

	public TestItem() {
	}
	//
	//	public TestItemStructure getTestItemStructure() {
	//		return testItemStructure;
	//	}
	//
	//	public void setTestItemStructure(TestItemStructure testItemStructure) {
	//		this.testItemStructure = testItemStructure;
	//	}

	public TestItemResults getTestItemResults() {
		return testItemResults;
	}

	public void setTestItemResults(TestItemResults testItemResults) {
		this.testItemResults = testItemResults;
	}

	public Set<TestItemTag> getTags() {
		return tags;
	}

	public void setTags(Set<TestItemTag> tags) {
		ofNullable(this.tags).ifPresent(it -> {
			it.clear();
			it.addAll(tags);
		});
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public Parameter[] getParameters() {
		return parameters;
	}

	public void setParameters(Parameter[] parameters) {
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
		return "TestItem{" + "id=" + id + ", name='" + name + '\'' + ", type=" + type + ", startTime=" + startTime + ", description='"
				+ description + '\'' + ", lastModified=" + lastModified + ", parameters=" + Arrays.toString(parameters) + ", uniqueId='"
				+ uniqueId + '\'' + ", tags=" + tags + '}';
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
		return Objects.equals(id, testItem.id) && Objects.equals(name, testItem.name) && type == testItem.type && Objects.equals(
				startTime, testItem.startTime) && Objects.equals(description, testItem.description) && Objects.equals(
				lastModified, testItem.lastModified) && Arrays.equals(parameters, testItem.parameters) && Objects.equals(
				uniqueId, testItem.uniqueId) && Objects.equals(tags, testItem.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, startTime, description, lastModified, parameters, uniqueId, tags);
	}

}
