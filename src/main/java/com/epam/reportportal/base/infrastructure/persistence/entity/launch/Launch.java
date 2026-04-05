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

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Pavel Bortnik
 */

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "launch", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "number", "project_id"})}, indexes = {
    @Index(name = "launch_pk", unique = true, columnList = "id ASC"),
    @Index(name = "unq_name_number", unique = true, columnList = "name ASC, number ASC, project_id ASC")})
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
  @Convert(converter = JpaInstantConverter.class)
  private Instant startTime;

  @Column(name = "end_time")
  @Convert(converter = JpaInstantConverter.class)
  private Instant endTime;

  @Column(name = "number", nullable = false, precision = 32)
  private Long number;

  @Column(name = "has_retries")
  private boolean hasRetries;

  @Column(name = "rerun")
  private boolean rerun;

  @Column(name = "last_modified", nullable = false)
  @LastModifiedDate
  @Convert(converter = JpaInstantConverter.class)
  private Instant lastModified;

  @Column(name = "mode", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private LaunchModeEnum mode;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private StatusEnum status;

  @Column(name = "retention_policy", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private RetentionPolicyEnum retentionPolicy = RetentionPolicyEnum.REGULAR;

  @Column(name = "launch_type")
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private LaunchTypeEnum launchType;

  @Column(name = "display_id")
  private String displayId;

  @OneToMany(mappedBy = "launch", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @Fetch(FetchMode.JOIN)
  private Set<ItemAttribute> attributes = Sets.newHashSet();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @JoinColumn(name = "launch_id", insertable = false, updatable = false)
  private Set<Statistics> statistics = Sets.newHashSet();

  @OneToMany(mappedBy = "launch", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Log> logs = Sets.newHashSet();

  @Column(name = "approximate_duration")
  private double approximateDuration;

  @Column(name = "test_plan_id")
  private Long testPlanId;

  public Launch() {
  }

  public Launch(Long id) {
    this.id = id;
  }

  public Set<ItemAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<ItemAttribute> tags) {
    this.attributes.clear();
    this.attributes.addAll(tags);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isRerun() {
    return rerun;
  }

  public void setRerun(boolean rerun) {
    this.rerun = rerun;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Set<Statistics> getStatistics() {
    return statistics;
  }

  public void setStatistics(Set<Statistics> statistics) {
    this.statistics = statistics;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public Long getNumber() {
    return number;
  }

  public void setNumber(Long number) {
    this.number = number;
  }

  public boolean isHasRetries() {
    return hasRetries;
  }

  public void setHasRetries(boolean hasRetries) {
    this.hasRetries = hasRetries;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
  }

  public LaunchModeEnum getMode() {
    return mode;
  }

  public void setMode(LaunchModeEnum mode) {
    this.mode = mode;
  }

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Set<Log> getLogs() {
    return logs;
  }

  public void setLogs(Set<Log> logs) {
    this.logs = logs;
  }

  public double getApproximateDuration() {
    return approximateDuration;
  }

  public void setApproximateDuration(double approximateDuration) {
    this.approximateDuration = approximateDuration;
  }

  public RetentionPolicyEnum getRetentionPolicy() {
    return retentionPolicy;
  }

  public void setRetentionPolicy(RetentionPolicyEnum retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

  public LaunchTypeEnum getLaunchType() {
    return launchType;
  }

  public void setLaunchType(LaunchTypeEnum launchType) {
    this.launchType = launchType;
  }

  public Long getTestPlanId() {
    return testPlanId;
  }

  public void setTestPlanId(Long testPlanId) {
    this.testPlanId = testPlanId;
  }

  public String getDisplayId() {
    return displayId;
  }

  public void setDisplayId(String displayId) {
    this.displayId = displayId;
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
