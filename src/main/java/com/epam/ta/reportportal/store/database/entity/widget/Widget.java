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

package com.epam.ta.reportportal.store.database.entity.widget;

import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "widget", schema = "public")
public class Widget implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "widget_type")
	private String widgetType;

	@Column(name = "items_count")
	private int itemsCount;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "content_field", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "field")
	private List<String> contentFields;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "widget_id")
	private Set<WidgetOption> widgetOptions = Sets.newHashSet();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

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

	public String getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(String widgetType) {
		this.widgetType = widgetType;
	}

	public int getItemsCount() {
		return itemsCount;
	}

	public void setItemsCount(int itemsCount) {
		this.itemsCount = itemsCount;
	}

	public List<String> getContentFields() {
		return contentFields;
	}

	public void setContentFields(List<String> contentFields) {
		this.contentFields = contentFields;
	}

	public Set<WidgetOption> getWidgetOptions() {
		return widgetOptions;
	}

	public void setWidgetOptions(Set<WidgetOption> widgetOptions) {
		this.widgetOptions = widgetOptions;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
