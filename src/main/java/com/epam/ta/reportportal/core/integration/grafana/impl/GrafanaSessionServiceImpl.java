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

package com.epam.ta.reportportal.core.integration.grafana.impl;

import com.epam.ta.reportportal.core.integration.grafana.GrafanaSessionService;
import com.epam.ta.reportportal.dao.GrafanaSessionRepository;
import com.epam.ta.reportportal.entity.integration.grafana.GrafanaSession;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed implementation of {@link GrafanaSessionService}.
 *
 * @author Siarhei Hrabko
 */
@Service
public class GrafanaSessionServiceImpl implements GrafanaSessionService {

  private final GrafanaSessionRepository grafanaSessionRepository;
  private final Duration ttl;

  public GrafanaSessionServiceImpl(GrafanaSessionRepository grafanaSessionRepository,
      @Value("${rp.grafana.session.ttl:PT15M}") Duration ttl) {
    this.grafanaSessionRepository = grafanaSessionRepository;
    this.ttl = ttl;
  }

  @Override
  @Transactional
  public UUID create(String subject) {
    if (StringUtils.isBlank(subject)) {
      throw new IllegalArgumentException("subject must not be blank");
    }
    GrafanaSession session =
        new GrafanaSession(UUID.randomUUID(), subject, Instant.now().plus(ttl));
    return grafanaSessionRepository.save(session).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isValid(UUID id) {
    return id != null && grafanaSessionRepository.existsByIdAndExpiresAtAfter(id, Instant.now());
  }

  @Override
  @Transactional
  public void revokeForSubject(String subject) {
    if (StringUtils.isBlank(subject)) {
      return;
    }
    grafanaSessionRepository.deleteBySubject(subject);
  }
}
