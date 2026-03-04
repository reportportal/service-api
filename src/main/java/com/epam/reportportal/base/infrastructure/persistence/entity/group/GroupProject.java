/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.group;

import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GroupProject entity.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 * @see GroupProjectId
 * @see Group
 * @see Project
 * @see ProjectRole
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups_projects", schema = "public")
public class GroupProject {

  @EmbeddedId
  private GroupProjectId id;

  @Enumerated(EnumType.STRING)
  @Column(name = "project_role")
  private ProjectRole projectRole;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("groupId")
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("projectId")
  private Project project;

  /**
   * Constructor for GroupProject entity.
   *
   * @param group       {@link Group}
   * @param project     {@link Project}
   * @param projectRole {@link ProjectRole}
   */
  public GroupProject(
      @NotNull Group group,
      @NotNull Project project,
      ProjectRole projectRole
  ) {
    this.id = new GroupProjectId(group.getId(), project.getId());
    this.group = group;
    this.project = project;
    this.projectRole = projectRole;
  }

  /**
   * Set the created_at and updated_at fields on entity creation.
   */
  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  /**
   * Updates the updated_at field on entity update.
   */
  @PreUpdate
  protected void onUpdated() {
    this.updatedAt = Instant.now();
  }
}
