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

package com.epam.reportportal.base.core.integration.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.base.core.integration.util.IntegrationService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CreateIntegrationHandlerImplTest {

  @Mock
  private Map<String, IntegrationService> integrationServiceMapping;
  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private IntegrationTypeRepository integrationTypeRepository;
  @Mock
  private IntegrationService basicIntegrationService;
  @Mock
  private CreateAuthIntegrationHandler createAuthIntegrationHandler;

  @InjectMocks
  private CreateIntegrationHandlerImpl handler;

  private static final Long EMAIL_TYPE_ID = 10L;
  private static final String PROJECT_KEY = "test-project";

  private ReportPortalUser rpUser;
  private IntegrationType emailType;

  @BeforeEach
  void setUp() {
    rpUser = getRpUser("testuser", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);

    emailType = new IntegrationType();
    emailType.setId(EMAIL_TYPE_ID);
    emailType.setName("email");
  }

  @Test
  @DisplayName("Should throw INTEGRATION_ALREADY_EXISTS when global email integration already exists")
  void createGlobalIntegrationWhenEmailAlreadyExistsShouldThrow() {
    // Given
    when(integrationTypeRepository.findByName("email")).thenReturn(Optional.of(emailType));
    when(integrationRepository.existsByTypeIdAndProjectIdIsNullAndOrganizationIdIsNull(EMAIL_TYPE_ID))
        .thenReturn(true);

    var request = new IntegrationRQ();
    request.setName("email-server-2");

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.createGlobalIntegration(request, "email", rpUser));
    assertEquals(ErrorType.INTEGRATION_ALREADY_EXISTS, ex.getErrorType());
  }

  @Test
  @DisplayName("Should throw INTEGRATION_ALREADY_EXISTS when project email integration already exists")
  void createProjectIntegrationWhenEmailAlreadyExistsShouldThrow() {
    // Given
    var project = new Project();
    project.setId(20L);
    project.setName(PROJECT_KEY);

    when(projectRepository.findByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
    when(integrationTypeRepository.findByName("email")).thenReturn(Optional.of(emailType));
    when(integrationRepository.existsByTypeIdAndProjectId(EMAIL_TYPE_ID, 20L)).thenReturn(true);

    var request = new IntegrationRQ();
    request.setName("email-server-2");

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.createProjectIntegration(PROJECT_KEY, request, "email", rpUser));
    assertEquals(ErrorType.INTEGRATION_ALREADY_EXISTS, ex.getErrorType());
  }
}
