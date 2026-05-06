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

package com.epam.reportportal.base.core.organization.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.IntegrationDeletedEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrganizationIntegrationHandlerImplTest {

  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private IntegrationTypeRepository integrationTypeRepository;
  @Mock
  private OrganizationRepositoryCustom organizationRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private OrganizationIntegrationHandlerImpl handler;

  private static final Long ORG_ID = 201L;
  private static final Long TYPE_ID = 1L;

  private MockedStatic<SecurityContextUtils> mockedSecurityContextUtils;

  private ReportPortalUser principal;
  private IntegrationType integrationType;
  private List<Integration> integrations;

  @BeforeEach
  void setUp() {
    mockedSecurityContextUtils = mockStatic(SecurityContextUtils.class);

    principal = getRpUser("testuser", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);

    integrationType = new IntegrationType();
    integrationType.setId(TYPE_ID);
    integrationType.setName("jira");

    Integration i1 = new Integration();
    i1.setId(901L);
    i1.setName("jira-integration-1");
    i1.setType(integrationType);

    Integration i2 = new Integration();
    i2.setId(902L);
    i2.setName("jira-integration-2");
    i2.setType(integrationType);

    integrations = List.of(i1, i2);
  }

  @AfterEach
  void tearDown() {
    mockedSecurityContextUtils.close();
  }

  @Test
  @DisplayName("Should delete integrations matching type and publish one event per integration")
  void deleteOrganizationIntegrationsWhenTypeProvidedShouldDeleteAndPublishEvents() {
    // Given
    mockedSecurityContextUtils.when(SecurityContextUtils::getPrincipal).thenReturn(principal);
    when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(new Organization()));
    when(integrationTypeRepository.findByName("jira")).thenReturn(Optional.of(integrationType));
    when(integrationRepository.findAllByOrganizationIdAndTypeId(ORG_ID, TYPE_ID)).thenReturn(integrations);

    // When
    handler.deleteOrganizationIntegrations(ORG_ID, "jira");

    // Then
    verify(integrationRepository).deleteAll(integrations);
    verify(eventPublisher, times(2)).publishEvent(any(IntegrationDeletedEvent.class));
  }

  @Test
  @DisplayName("Should delete all org integrations when type is not specified")
  void deleteOrganizationIntegrationsWhenNoTypeShouldDeleteAllAndPublishEvents() {
    // Given
    mockedSecurityContextUtils.when(SecurityContextUtils::getPrincipal).thenReturn(principal);
    when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(new Organization()));
    when(integrationRepository.findAllByOrganizationId(eq(ORG_ID), any(Pageable.class)))
        .thenReturn(new PageImpl<>(integrations));

    // When
    handler.deleteOrganizationIntegrations(ORG_ID, null);

    // Then
    verify(integrationRepository).deleteAll(integrations);
    verify(eventPublisher, times(2)).publishEvent(any(IntegrationDeletedEvent.class));
  }

  @Test
  @DisplayName("Should throw INTEGRATION_NOT_FOUND when type does not exist")
  void deleteOrganizationIntegrationsWhenTypeNotFoundShouldThrow() {
    // Given
    mockedSecurityContextUtils.when(SecurityContextUtils::getPrincipal).thenReturn(principal);
    when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(new Organization()));
    when(integrationTypeRepository.findByName("unknown")).thenReturn(Optional.empty());

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteOrganizationIntegrations(ORG_ID, "unknown"));
    assertEquals(ErrorType.INTEGRATION_NOT_FOUND, ex.getErrorType());
  }

  @Test
  @DisplayName("Should throw ORGANIZATION_NOT_FOUND when org does not exist")
  void deleteOrganizationIntegrationsWhenOrgNotFoundShouldThrow() {
    // Given
    when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.empty());

    // When & Then
    var ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteOrganizationIntegrations(ORG_ID, "jira"));
    assertEquals(ErrorType.ORGANIZATION_NOT_FOUND, ex.getErrorType());
  }
}
