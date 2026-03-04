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

package com.epam.reportportal.base.infrastructure.persistence.entity.filter;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;


/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "filter")

public class UserFilter extends OwnedEntity implements Serializable {

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "target")
  private ObjectType targetClass;

  @Column(name = "description")
  private String description;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "filter_id")
  private Set<FilterCondition> filterCondition = Sets.newHashSet();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "filter_id")
  @OrderBy(value = "id")
  private Set<FilterSort> filterSorts = Sets.newLinkedHashSet();

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ObjectType getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(ObjectType targetClass) {
    this.targetClass = targetClass;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
