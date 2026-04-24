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

package com.epam.reportportal.base.infrastructure.persistence.entity.project;

import com.epam.reportportal.base.infrastructure.persistence.entity.attribute.Attribute;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Key part of a project attribute: attribute id, project id, and system flag.
 *
 * @author Andrey Plisunov
 */
public class ProjectAttributeKey implements Serializable {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "attribute_id", nullable = false, insertable = false, updatable = false)
  private Attribute attribute;

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(project).append(attribute).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ProjectAttributeKey)) {
      return false;
    }

    ProjectAttributeKey projectAttributeKey = (ProjectAttributeKey) obj;

    return new EqualsBuilder().append(project.getId(), projectAttributeKey.project.getId())
        .append(attribute.getId(), projectAttributeKey.attribute.getId())
        .isEquals();
  }
}
