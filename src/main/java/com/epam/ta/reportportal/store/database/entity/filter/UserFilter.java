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

package com.epam.ta.reportportal.store.database.entity.filter;

import com.epam.ta.reportportal.store.commons.JsonbUserType;
import com.epam.ta.reportportal.store.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.google.common.collect.Sets;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */

@Entity
@TypeDef(name = "jsonb", typeClass = JsonbUserType.class)
@Table(name = "filter", schema = "public")
public class UserFilter implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "name")
	private String name;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "target")
	private String targetClass;

	@Column(name = "description")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "filter_id")
	private Set<FilterCondition> filterCondition = Sets.newHashSet();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "filter_id")
	private Set<FilterSort> filterSorts = Sets.newLinkedHashSet();

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "widget_filter", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "widget_id"))
	private Set<Widget> widgets = Sets.newHashSet();

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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<FilterCondition> getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(Set<FilterCondition> filterCondition) {
		this.filterCondition = filterCondition;
	}

	public Set<FilterSort> getFilterSorts() {
		return filterSorts;
	}

	public void setFilterSorts(Set<FilterSort> filterSorts) {
		this.filterSorts = filterSorts;
	}

	public Set<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(Set<Widget> widgets) {
		this.widgets = widgets;
	}
}
