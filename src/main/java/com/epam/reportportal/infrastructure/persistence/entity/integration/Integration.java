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

package com.epam.reportportal.infrastructure.persistence.entity.integration;

import com.epam.reportportal.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Yauheni_Martynau
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "integration", schema = "public")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Integration implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne
  @JoinColumn(name = "type")
  private IntegrationType type;

  @Type(IntegrationParams.class)
  @Column(name = "params")
  private IntegrationParams params;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "creator")
  private String creator;

  @CreatedDate
  @Column(name = "creation_date")
  @Convert(converter = JpaInstantConverter.class)
  private Instant creationDate;

  public Integration(Long id, Project project, IntegrationType type, IntegrationParams params,
      Instant creationDate) {
    this.id = id;
    this.project = project;
    this.type = type;
    this.params = params;
    this.creationDate = creationDate;
  }

  public Integration() {
  }

}

