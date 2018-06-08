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

import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Pavel Bortnik1
 */
@Entity
@Table(name = "filter")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Filter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "target")
	private String targetClass;

	@Column(name = "description")
	private String description;

	@ManyToOne
	@JoinColumn(name = "project_id")
	private Project project;

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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Set<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(Set<Widget> widgets) {
		this.widgets = widgets;
	}
}
