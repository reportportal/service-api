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

import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class ProjectIssueTypeKey implements Serializable {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "issue_type_id", nullable = false, insertable = false, updatable = false)
  private IssueType issueType;

  public ProjectIssueTypeKey() {
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public IssueType getIssueType() {
    return issueType;
  }

  public void setIssueType(IssueType issueType) {
    this.issueType = issueType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectIssueTypeKey that = (ProjectIssueTypeKey) o;
    return Objects.equals(project, that.project) && Objects.equals(issueType, that.issueType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, issueType);
  }
}
