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

package com.epam.reportportal.base.infrastructure.persistence.entity.activity;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;


/**
 * Activity table entity
 *
 * @author Ryhor Kukharenka
 */
@Entity
@Table(name = "activity", schema = "public")
@Getter
@Setter
@ToString
public class Activity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;

  @Column(name = "action", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private EventAction action;

  @Column(name = "event_name", nullable = false)
  private String eventName;

  @Column(name = "priority", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private EventPriority priority;

  @Column(name = "object_id")
  private Long objectId;

  @Column(name = "object_name", nullable = false)
  private String objectName;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "object_type", nullable = false)
  private EventObject objectType;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "organization_id")
  private Long organizationId;

  @Transient
  private String projectName;

  @Column(name = "details")
  @Type(ActivityDetails.class)
  private ActivityDetails details;

  @Column(name = "subject_id", precision = 32)
  private Long subjectId;

  @Column(name = "subject_name", nullable = false)
  private String subjectName;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "subject_type", nullable = false)
  private EventSubject subjectType;

  /**
   * A boolean to decide whether to save the event.
   */
  @Transient
  private boolean isSavedEvent;

  public Activity() {
    this.isSavedEvent = true;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Activity activity = (Activity) o;
    return Objects.equals(id, activity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
