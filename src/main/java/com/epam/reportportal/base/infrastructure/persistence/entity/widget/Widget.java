/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.entity.widget;

import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.google.common.collect.Sets;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;


/**
 * User-defined or system widget: type, filters, and links to content and shared filters.
 *
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "widget")
public class Widget extends OwnedEntity implements Serializable {

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "widget_type")
  private String widgetType;

  @Column(name = "items_count")
  private int itemsCount;

  @ManyToMany
  @JoinTable(name = "widget_filter", joinColumns = @JoinColumn(name = "widget_id"), inverseJoinColumns = @JoinColumn(name = "filter_id"))
  private Set<UserFilter> filters;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "content_field", joinColumns = @JoinColumn(name = "id"))
  @Column(name = "field")
  @OrderBy(value = "id")
  private Set<String> contentFields = Sets.newLinkedHashSet();

  @Type(WidgetOptions.class)
  @Column(name = "widget_options")
  private WidgetOptions widgetOptions;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "widget")
  @Fetch(value = FetchMode.JOIN)
  private Set<DashboardWidget> dashboardWidgets = Sets.newHashSet();

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

  public Set<String> getContentFields() {
    return contentFields;
  }

  public void setContentFields(Set<String> contentFields) {
    this.contentFields = contentFields;
  }

  public WidgetOptions getWidgetOptions() {
    return widgetOptions;
  }

  public void setWidgetOptions(WidgetOptions widgetOptions) {
    this.widgetOptions = widgetOptions;
  }

  public Set<DashboardWidget> getDashboardWidgets() {
    return dashboardWidgets;
  }

  public void setDashboardWidgets(Set<DashboardWidget> dashboardWidgets) {
    this.dashboardWidgets = dashboardWidgets;
  }

  public Set<UserFilter> getFilters() {
    return filters;
  }

  public void setFilters(Set<UserFilter> filters) {
    this.filters = filters;
  }
}
