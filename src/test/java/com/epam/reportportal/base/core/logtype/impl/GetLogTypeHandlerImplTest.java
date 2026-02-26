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

package com.epam.reportportal.base.core.logtype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.GetLogTypes200Response;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetLogTypeHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LogTypeRepository logTypeRepository;

  @InjectMocks
  private GetLogTypeHandlerImpl handler;

  @Test
  void getLogTypesWhenProjectExistsShouldMapAndReturnSeveralItems() {
    // Given
    long projectId = 1L;
    String projectKey = "default_personal";
    final Project project = new Project(projectId, projectKey);

    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
    when(logTypeRepository.findByProjectId(projectId)).thenReturn(getBuildLogTypeModels());

    // When
    GetLogTypes200Response response = handler.getLogTypes(projectKey);

    // Then
    List<LogTypeResponse> items = response.getItems();
    assertEquals(2, items.size());
    assertEquals("INFO", items.get(0).getName());
    assertEquals(20000, items.get(0).getLevel());
    assertEquals("ERROR", items.get(1).getName());
    assertEquals(40000, items.get(1).getLevel());
  }

  @Test
  void getLogTypesWhenProjectMissingShouldThrowProjectNotFound() {
    // Given
    String projectName = "unknown";
    when(projectRepository.findByKey(projectName)).thenReturn(Optional.empty());

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.getLogTypes(projectName));

    // Then
    assertEquals(ErrorType.PROJECT_NOT_FOUND, ex.getErrorType());
  }

  private List<ProjectLogType> getBuildLogTypeModels() {
    ProjectLogType info = new ProjectLogType();
    info.setId(10L);
    info.setName("INFO");
    info.setLevel(20000);
    info.setFilterable(true);
    info.setSystem(true);
    info.setLabelColor("#4DB6AC");
    info.setBackgroundColor("#FFFFFF");
    info.setTextColor("#445A47");
    info.setTextStyle("normal");

    ProjectLogType error = new ProjectLogType();
    error.setId(11L);
    error.setName("ERROR");
    error.setLevel(40000);
    error.setFilterable(true);
    error.setSystem(true);
    error.setLabelColor("#4DB6AC");
    error.setBackgroundColor("#FFFFFF");
    error.setTextColor("#445A47");
    error.setTextStyle("normal");

    return List.of(info, error);
  }

}
