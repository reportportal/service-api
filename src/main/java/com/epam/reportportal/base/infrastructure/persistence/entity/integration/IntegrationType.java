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

package com.epam.reportportal.base.infrastructure.persistence.entity.integration;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.google.common.collect.Sets;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;

/**
 * Global integration plugin type definition (BTS, mail, auth, and metadata).
 *
 * @author Yauheni_Martynau
 */
@Entity
@Table(name = "integration_type", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationType implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, precision = 64)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "plugin_type", nullable = false)
  private PluginTypeEnum pluginType;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "auth_flow")
  private IntegrationAuthFlowEnum authFlow;

  @CreatedDate
  @Column(name = "creation_date", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant creationDate;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "group_type", nullable = false)
  private IntegrationGroupEnum integrationGroup;

  @Column(name = "enabled")
  private boolean enabled;

  @Type(IntegrationTypeDetails.class)
  @Column(name = "details")
  private IntegrationTypeDetails details;

  @OneToMany(mappedBy = "type", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Integration> integrations = Sets.newHashSet();

}
