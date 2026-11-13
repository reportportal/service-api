/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.integration.grafana;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Gate row backing the {@code /grafana/*} nginx {@code auth_request} check. A row's existence (and non-expiry) is what
 * nginx treats as "this browser session may reach Grafana"; the value itself carries no credential.
 *
 * @author Siarhei Hrabko
 */
@Entity
@Table(name = "grafana_session", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrafanaSession {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "expires_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant expiresAt;
}
