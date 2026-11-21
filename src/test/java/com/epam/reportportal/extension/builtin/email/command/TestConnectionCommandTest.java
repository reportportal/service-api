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

package com.epam.reportportal.extension.builtin.email.command;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.integration.util.EmailServerIntegrationService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class TestConnectionCommandTest {

  private final ProjectRepository projectRepository = mock(ProjectRepository.class);
  private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
  private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
  private final ProjectUserRepository projectUserRepository = mock(ProjectUserRepository.class);
  private final EmailServerIntegrationService emailServerIntegrationService =
      mock(EmailServerIntegrationService.class);

  private final TestConnectionCommand command = new TestConnectionCommand(projectRepository, organizationRepository,
      organizationUserRepository, projectUserRepository, emailServerIntegrationService);

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private static Integration projectIntegration() {
    Project project = new Project();
    project.setId(10L);
    Integration integration = new Integration();
    integration.setProject(project);
    return integration;
  }

  private static void authenticate(UserRole role) {
    ReportPortalUser user = getRpUser("user", role, OrganizationRole.MEMBER, ProjectRole.VIEWER, 10L);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @Test
  void nonMemberIsDeniedBeforeConnecting() {
    authenticate(UserRole.USER);
    Project project = new Project();
    project.setId(10L);
    project.setOrganizationId(1L);
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
    when(organizationUserRepository.findByUserIdAndOrganization_Id(1L, 1L)).thenReturn(Optional.empty());

    Integration integration = projectIntegration();
    PluginCommandRQ rq = new PluginCommandRQ().context(new PluginCommandContext());
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> command.executeCommand(integration, rq));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(emailServerIntegrationService, never()).checkConnection(any());
  }

  @Test
  void adminConnects() {
    authenticate(UserRole.ADMINISTRATOR);
    Integration integration = projectIntegration();
    when(emailServerIntegrationService.checkConnection(integration)).thenReturn(true);

    assertTrue(command.executeCommand(integration, new PluginCommandRQ()));
  }
}
