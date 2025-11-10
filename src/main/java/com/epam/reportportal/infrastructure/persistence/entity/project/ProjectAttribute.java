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

package com.epam.reportportal.infrastructure.persistence.entity.project;

import com.epam.reportportal.infrastructure.persistence.entity.attribute.Attribute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Andrey Plisunov
 */
@Entity
@Table(name = "project_attribute")
@IdClass(ProjectAttributeKey.class)
public class ProjectAttribute implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @ManyToOne
  @JoinColumn(name = "attribute_id")
  private Attribute attribute;

  @Column(name = "value")
  private String value;

  @Id
  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  public ProjectAttribute() {
  }

  public ProjectAttribute(Attribute attribute, String value, Project project) {
    this.attribute = attribute;
    this.value = value;
    this.project = project;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public ProjectAttribute withAttribute(Attribute attribute) {
    this.attribute = attribute;
    return this;
  }

  public ProjectAttribute withProject(Project project) {
    this.project = project;
    return this;
  }

  public ProjectAttribute withValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectAttribute that = (ProjectAttribute) o;
    return Objects.equals(attribute, that.attribute) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {

    return Objects.hash(attribute, value);
  }
}
