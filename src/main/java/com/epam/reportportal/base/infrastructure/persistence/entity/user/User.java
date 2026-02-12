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

package com.epam.reportportal.base.infrastructure.persistence.entity.user;

import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupUser;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * @author Andrei Varabyeu
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", schema = "public")
@DynamicInsert
public class User implements Serializable {

  private static final long serialVersionUID = 923392981;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "uuid")
  private UUID uuid;

  @Column(name = "external_id")
  private String externalId;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "login")
  private String login;

  @Column(name = "password")
  private String password;

  @Column(name = "email")
  private String email;

  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private UserRole role;

  @Column(name = "full_name")
  private String fullName;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "expired")
  private boolean isExpired;

  @Column(name = "metadata")
  @Type(Metadata.class)
  private Metadata metadata;

  @Column(name = "attachment")
  private String attachment;

  @Column(name = "attachment_thumbnail")
  private String attachmentThumbnail;

  @Column(name = "type")
  private UserType userType;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.PERSIST,
      CascadeType.MERGE, CascadeType.REFRESH})
  private Set<ProjectUser> projects = Sets.newHashSet();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.PERSIST,
      CascadeType.MERGE, CascadeType.REFRESH})
  private Set<OrganizationUser> organizationUsers = Sets.newHashSet();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.PERSIST,
      CascadeType.MERGE, CascadeType.REFRESH})
  private Set<GroupUser> groups = Sets.newHashSet();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(id, user.id)
        && Objects.equals(uuid, user.uuid)
        && Objects.equals(login, user.login)
        && Objects.equals(email, user.email)
        ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uuid, login, email);
  }
}
