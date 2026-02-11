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

import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProject;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.pattern.PatternTemplate;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

/**
 * @author Ivan Budayeu
 */
@Entity
@Table(name = "project", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project implements Serializable {

  private static final long serialVersionUID = -263516611;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "name")
  private String name;

  @OneToMany(mappedBy = "project", cascade = {CascadeType.PERSIST}, fetch = FetchType.LAZY)
  @OrderBy("creationDate desc")
  private Set<Integration> integrations = Sets.newHashSet();

  @OneToMany(mappedBy = "project", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
  private Set<ProjectAttribute> projectAttributes = Sets.newHashSet();

  @OneToMany(mappedBy = "project", cascade = {CascadeType.PERSIST,
      CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
  @OrderBy(value = "issue_type_id")
  private Set<ProjectIssueType> projectIssueTypes = Sets.newHashSet();

  @OneToMany(mappedBy = "project", cascade = {
      CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<SenderCase> senderCases = Sets.newHashSet();

  @Column(name = "created_at")
  private Instant creationDate;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata")
  @Type(Metadata.class)
  private Metadata metadata;

  // TODO: rename to meaningful variable. eg. orgSlug, orgKey or else
  @Column(name = "organization")
  private String org;

  @Column(name = "organization_id", nullable = false)
  private Long organizationId;

  @Column(name = "key")
  private String key;

  @Column(name = "slug")
  private String slug;

  @Column(name = "allocated_storage", updatable = false)
  private long allocatedStorage;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.PERSIST)
  private Set<ProjectUser> users = Sets.newHashSet();

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "project_id", updatable = false)
  @OrderBy
  private Set<PatternTemplate> patternTemplates = Sets.newHashSet();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.PERSIST)
  private Set<GroupProject> groups = Sets.newHashSet();

  public Project(Long id, String name) {
    this.id = id;
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Project project = (Project) o;
    return Objects.equals(name, project.name)
        && Objects.equals(key, project.key)
        && Objects.equals(organizationId, project.organizationId)
        && Objects.equals(allocatedStorage, project.allocatedStorage)
        && Objects.equals(creationDate, project.creationDate)
        && Objects.equals(metadata, project.metadata);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, creationDate, metadata, allocatedStorage);
  }

}
