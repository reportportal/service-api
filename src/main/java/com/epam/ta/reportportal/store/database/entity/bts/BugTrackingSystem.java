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
	private Integer id;

	@Column(name = "url", nullable = false)
	private String url;

	@Column(name = "project_id")
	private Long projectId;

	@Column(name = "type")
	private String btsType;

	@Column(name = "bts_project", nullable = false)
	private String btsProject;

	@OneToOne(mappedBy = "bugTrackingSystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private BugTrackingSystemAuth auth;

	@OneToMany(mappedBy = "bugTrackingSystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<DefectFormField> defectFormFields;

	public BugTrackingSystem() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Set<DefectFormField> getDefectFormFields() {
		return defectFormFields;
	}

	public void setDefectFormFields(Set<DefectFormField> defectFormFields) {
		this.defectFormFields = defectFormFields;
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

	public BugTrackingSystemAuth getAuth() {
		return auth;
	}

	public void setAuth(BugTrackingSystemAuth auth) {
		this.auth = auth;
		this.auth.setBugTrackingSystem(this);
	}
}
