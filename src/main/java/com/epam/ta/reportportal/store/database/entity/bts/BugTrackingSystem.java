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

package com.epam.ta.reportportal.store.database.entity.bts;

import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "bug_tracking_system", schema = "public", indexes = {
		@Index(name = "bug_tracking_system_pk", unique = true, columnList = "id ASC") })
public class BugTrackingSystem implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 32)
	private Long id;

	@Column(name = "url", nullable = false)
	private String url;

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "type")
	private String btsType;

	@Column(name = "bts_project", nullable = false)
	private String btsProject;

	@OneToMany(mappedBy = "bugTrackingSystem", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<DefectFormField> defectFormFields = Sets.newHashSet();

	public BugTrackingSystem() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Set<DefectFormField> getDefectFormFields() {
		return defectFormFields;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBtsType() {
		return btsType;
	}

	public void setBtsType(String btsType) {
		this.btsType = btsType;
	}

	public String getBtsProject() {
		return btsProject;
	}

	public void setBtsProject(String btsProject) {
		this.btsProject = btsProject;
	}

}
