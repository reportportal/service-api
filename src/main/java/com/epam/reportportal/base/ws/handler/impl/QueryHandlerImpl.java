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

package com.epam.reportportal.base.ws.handler.impl;

import com.epam.reportportal.base.infrastructure.persistence.dao.FilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.handler.QueryHandler;
import com.epam.reportportal.base.ws.rabbit.QueryRQ;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author Yauheni_Martynau
 */
@Service
public class QueryHandlerImpl implements QueryHandler {

  private final ProjectRepository projectRepository;
  private final IntegrationRepository integrationRepository;
  private final TestItemRepository testItemRepository;
  private final LogRepository logRepository;

  private Map<String, FilterableRepository> repositories;

  public QueryHandlerImpl(ProjectRepository projectRepository,
      IntegrationRepository integrationRepository,
      TestItemRepository testItemRepository, LogRepository logRepository) {

    this.projectRepository = projectRepository;
    this.integrationRepository = integrationRepository;
    this.testItemRepository = testItemRepository;
    this.logRepository = logRepository;

    repositories = ImmutableMap.<String, FilterableRepository>builder()
        .put(Project.class.getSimpleName(), projectRepository)
        .put(Integration.class.getSimpleName(), integrationRepository)
        .put(TestItem.class.getSimpleName(), testItemRepository)
        .put(Log.class.getSimpleName(), logRepository)
        .build();
  }

  @Override
  public Object find(QueryRQ queryRQ) {

    return Optional.ofNullable(repositories.get(queryRQ.getEntity()))
        .map(repository -> repository.findByFilter(queryRQ.getFilter()))
        .orElseThrow(() -> new ReportPortalException("Repository not found"));
  }
}
