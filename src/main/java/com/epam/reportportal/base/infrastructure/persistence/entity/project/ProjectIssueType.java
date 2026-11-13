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

import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * BTS defect type linked to a project (JIRA-style locator and short name).
 *
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue_type_project")
@IdClass(ProjectIssueTypeKey.class)
public class ProjectIssueType implements Serializable {

  @Id
  @ManyToOne
  @JoinColumn(name = "issue_type_id")
  private IssueType issueType;

  @Id
  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  public IssueType getIssueType() {
    return issueType;
  }

  public void setIssueType(IssueType issueType) {
    this.issueType = issueType;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectIssueType that = (ProjectIssueType) o;
    return Objects.equals(issueType, that.issueType) && Objects.equals(project, that.project);
  }

  @Override
  public int hashCode() {

    return Objects.hash(issueType, project);
  }
}
