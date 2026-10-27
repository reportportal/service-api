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

package com.epam.reportportal.base.infrastructure.persistence.entity.launch;

import com.epam.reportportal.base.infrastructure.persistence.entity.Attribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A test run: name, times, status, and statistics for one project and user.
 *
 * @author Pavel Bortnik
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "launch", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "number", "project_id"})}, indexes = {
    @Index(name = "launch_pk", unique = true, columnList = "id ASC"),
    @Index(name = "unq_name_number", unique = true, columnList = "name ASC, number ASC, project_id ASC")})
@Getter
@Setter
@NoArgsConstructor
public class Launch implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "uuid", unique = true, nullable = false)
  private String uuid;

  @Column(name = "project_id", nullable = false, precision = 32)
  private Long projectId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "end_time")
  private Instant endTime;

  @Column(name = "number", nullable = false, precision = 32)
  private Long number;

  @Column(name = "has_retries")
  private boolean hasRetries;

  @Column(name = "rerun")
  private boolean rerun;

  @Column(name = "last_modified", nullable = false)
  @LastModifiedDate
  private Instant lastModified;

  @Column(name = "mode", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private LaunchModeEnum mode;

  @Column(name = "status", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private StatusEnum status;

  @Column(name = "retention_policy", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private RetentionPolicyEnum retentionPolicy = RetentionPolicyEnum.REGULAR;

  @Column(name = "launch_type", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private LaunchTypeEnum launchType = LaunchTypeEnum.AUTOMATION;

  @Column(name = "display_id")
  private String displayId;

  @OneToMany(mappedBy = "launch", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @Fetch(FetchMode.JOIN)
  private Set<LaunchAttribute> attributes = Sets.newHashSet();

  @OneToMany(mappedBy = "launch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  private Set<LaunchStatistics> statistics = Sets.newHashSet();

  @OneToMany(mappedBy = "launch", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Log> logs = Sets.newHashSet();

  @Column(name = "approximate_duration")
  private double approximateDuration;

  @Column(name = "test_plan_id")
  private Long testPlanId;

  public Launch(Long id) {
    this.id = id;
  }

  public void setAttributes(Collection<? extends Attribute> tags) {
    this.attributes.clear();
    tags.forEach(attribute -> {
      if (attribute instanceof LaunchAttribute launchAttribute) {
        this.attributes.add(launchAttribute);
      } else {
        LaunchAttribute launchAttribute =
            new LaunchAttribute(attribute.getKey(), attribute.getValue(), attribute.isSystem());
        launchAttribute.setLaunch(this);
        this.attributes.add(launchAttribute);
      }
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Launch launch = (Launch) o;
    return hasRetries == launch.hasRetries && rerun == launch.rerun && Objects.equals(uuid,
        launch.uuid
    ) && Objects.equals(projectId, launch.projectId) && Objects.equals(
        name, launch.name) && Objects.equals(description, launch.description) && Objects.equals(
        startTime, launch.startTime) && Objects.equals(endTime, launch.endTime) && Objects.equals(
        number, launch.number) && mode == launch.mode && status == launch.status
        && retentionPolicy == launch.retentionPolicy && launchType == launch.launchType
        && Objects.equals(displayId, launch.displayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, projectId, name, description, startTime, endTime, number, hasRetries,
        rerun, mode, status, retentionPolicy, launchType, displayId
    );
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Launch{");
    sb.append("id=").append(id);
    sb.append(", uuid='").append(uuid).append('\'');
    sb.append(", projectId=").append(projectId);
    sb.append(", userId=").append(userId);
    sb.append(", name='").append(name).append('\'');
    sb.append(", description='").append(description).append('\'');
    sb.append(", startTime=").append(startTime);
    sb.append(", endTime=").append(endTime);
    sb.append(", number=").append(number);
    sb.append(", hasRetries=").append(hasRetries);
    sb.append(", rerun=").append(rerun);
    sb.append(", lastModified=").append(lastModified);
    sb.append(", mode=").append(mode);
    sb.append(", status=").append(status);
    sb.append(", launchType=").append(launchType);
    sb.append(", attributes=").append(attributes);
    sb.append(", statistics=").append(statistics);
    sb.append(", approximateDuration=").append(approximateDuration);
    sb.append(", retentionPolicy=").append(retentionPolicy);
    sb.append(", testPlanId=").append(testPlanId);
    sb.append(", displayId=").append(displayId);
    sb.append('}');
    return sb.toString();
  }
}
