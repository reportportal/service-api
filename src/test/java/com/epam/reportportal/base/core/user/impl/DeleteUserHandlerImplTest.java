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

package com.epam.reportportal.base.core.user.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.OrganizationDeletedEvent;
import com.epam.reportportal.base.core.events.domain.ProjectDeletedEvent;
import com.epam.reportportal.base.core.events.domain.UserDeletedEvent;
import com.epam.reportportal.base.core.remover.ContentRemover;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.email.strategy.EmailNotificationStrategy;
import com.epam.reportportal.base.util.email.strategy.EmailTemplate;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerImplTest {

  @Mock
  private UserRepository repository;

  @Mock
  private UserBinaryDataService dataStore;

  @Mock
  private ContentRemover<User> userContentRemover;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private Map<EmailTemplate, EmailNotificationStrategy> emailNotificationStrategyMapping;

  @Mock
  private EmailNotificationStrategy emailNotificationStrategy;

  @Mock
  private OrganizationUserRepository organizationUserRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private ProjectUserRepository projectUserRepository;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private DeleteUserHandlerImpl handler;

  @Test
  void deleteUser() {
    User user = new User();
    user.setId(2L);
    user.setLogin("test");

    doReturn(Optional.of(user)).when(repository).findById(2L);
    when(organizationUserRepository.findNonPersonalOrganizationIdsByUserId(user.getId())).thenReturn(
        Lists.newArrayList());
    when(projectRepository.findAllByUserLogin(user.getLogin())).thenReturn(Lists.newArrayList());
    when(organizationRepository.findByOwnerIdAndOrganizationType(user.getId(), OrganizationType.PERSONAL))
        .thenReturn(Optional.empty());
    doNothing().when(dataStore).deleteUserPhoto(any());
    when(emailNotificationStrategyMapping.get(any())).thenReturn(emailNotificationStrategy);
    doNothing().when(emailNotificationStrategy).sendEmail(any(), anyMap());
    doNothing().when(applicationEventPublisher).publishEvent(isA(UserDeletedEvent.class));

    handler.deleteUser(
        2L, getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L));

    verify(repository, times(1)).findById(2L);
    verify(dataStore, times(1)).deleteUserPhoto(any());

  }

  @Test
  void deleteNotExistedUser() {
    when(repository.findById(12345L)).thenReturn(Optional.empty());

    final ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.deleteUser(12345L,
            getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L)
        ));
    assertEquals("User '12345' not found.", exception.getMessage());
  }

  @Test
  void deleteOwnAccount() {
    User user = new User();
    user.setId(1L);

    doReturn(Optional.of(user)).when(repository).findById(1L);

    final ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.deleteUser(1L,
            getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
                1L)
        ));
    assertEquals("You do not have enough permissions. You cannot delete own account",
        exception.getMessage());

    verify(repository, times(1)).findById(1L);
    verify(repository, times(0)).delete(any(User.class));
  }

  @Test
  void deleteUserWithPersonalOrganizationShouldPublishOrgAndProjectEvents() {
    User user = new User();
    user.setId(2L);
    user.setLogin("test");

    Organization personalOrg = new Organization();
    personalOrg.setId(10L);
    personalOrg.setName("Personal Org");
    personalOrg.setOwnerId(2L);
    personalOrg.setOrganizationType(OrganizationType.PERSONAL);

    Project project = new Project();
    project.setId(100L);
    project.setName("Personal Project");
    project.setOrganizationId(10L);

    doReturn(Optional.of(user)).when(repository).findById(2L);
    when(organizationUserRepository.findNonPersonalOrganizationIdsByUserId(user.getId()))
        .thenReturn(Lists.newArrayList());
    when(projectRepository.findAllByUserLogin(user.getLogin())).thenReturn(Lists.newArrayList());
    when(organizationRepository.findByOwnerIdAndOrganizationType(user.getId(), OrganizationType.PERSONAL))
        .thenReturn(Optional.of(personalOrg));
    when(organizationUserRepository.findUserIdsByOrganizationId(10L)).thenReturn(List.of(2L));
    when(projectRepository.findAllByOrganizationId(10L)).thenReturn(List.of(project));
    when(projectUserRepository.findUserIdsByProjectId(100L)).thenReturn(List.of(2L));
    doNothing().when(dataStore).deleteUserPhoto(any());
    when(emailNotificationStrategyMapping.get(any())).thenReturn(emailNotificationStrategy);
    doNothing().when(emailNotificationStrategy).sendEmail(any(), anyMap());

    handler.deleteUser(
        2L, getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L));

    verify(organizationRepository).findByOwnerIdAndOrganizationType(eq(2L), eq(OrganizationType.PERSONAL));
    verify(applicationEventPublisher).publishEvent(argThat(event ->
        event instanceof ProjectDeletedEvent projectEvent
            && projectEvent.getOrganizationId() == null
            && projectEvent.getProjectId().equals(100L)));
    verify(applicationEventPublisher).publishEvent(isA(OrganizationDeletedEvent.class));
    verify(applicationEventPublisher).publishEvent(isA(UserDeletedEvent.class));
  }

}
