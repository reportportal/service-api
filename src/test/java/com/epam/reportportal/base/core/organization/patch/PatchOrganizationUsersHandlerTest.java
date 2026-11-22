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

package com.epam.reportportal.base.core.organization.patch;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

class PatchOrganizationUsersHandlerTest {

  private static final Long ORG_ID = 1L;

  private final OrganizationUserService organizationUserService = mock(OrganizationUserService.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final OrganizationRepositoryCustom organizationRepository = mock(OrganizationRepositoryCustom.class);
  private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  private PatchOrganizationUsersHandler handler;
  private Organization organization;

  private static OrganizationUser orgUser(long id, OrganizationRole role) {
    var user = new User();
    user.setId(id);
    var ou = new OrganizationUser();
    ou.setUser(user);
    ou.setOrganizationRole(role);
    return ou;
  }

  @BeforeEach
  void setUp() {
    handler = new PatchOrganizationUsersHandler(organizationUserService, JsonMapper.builder().build(),
        userRepository, organizationRepository, organizationUserRepository, eventPublisher);
    organization = new Organization();
    organization.setId(ORG_ID);
    organization.setName("org");
    organization.setOrganizationType(OrganizationType.INTERNAL);
    when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));

    var principal = getRpUser("manager", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, "x"));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private static PatchOperation remove(Object value) {
    var op = new PatchOperation();
    op.setValue(value);
    return op;
  }

  @Test
  void removeAllWithNullValueRemovesManagersToo() {
    when(organizationUserRepository.findUserIdsByOrganizationId(ORG_ID)).thenReturn(List.of(1L, 2L));

    handler.remove(remove(null), ORG_ID);

    verify(organizationUserService).unassignAllUsersFromOrganization(ORG_ID);
  }

  @Test
  void removeAllIsRefusedForPersonalOrganizationOwner() {
    organization.setOrganizationType(OrganizationType.PERSONAL);
    organization.setOwnerId(2L);
    when(organizationUserRepository.findUserIdsByOrganizationId(ORG_ID)).thenReturn(List.of(2L));

    var ex = assertThrows(ReportPortalException.class, () -> handler.remove(remove(null), ORG_ID));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(organizationUserService, never()).unassignAllUsersFromOrganization(ORG_ID);
  }

  @Test
  void explicitRemovalOfMemberIsAllowed() {
    when(organizationUserRepository.findUserIdsByOrganizationId(ORG_ID)).thenReturn(List.of(1L, 2L));

    handler.remove(remove(List.of(Map.of("id", 2))), ORG_ID);

    verify(organizationUserService).deleteByUserIdsAndOrganizationId(List.of(2L), ORG_ID);
  }

  @Test
  void replaceThatDemotesCallerManagerIsRefused() {
    var self = new User();
    self.setId(1L);
    when(organizationUserRepository.findUserIdsByOrganizationId(ORG_ID)).thenReturn(List.of(1L));
    when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(self));
    when(organizationUserRepository.findAllByOrganizationIdAndUserIdIn(ORG_ID, List.of(1L)))
        .thenReturn(List.of(orgUser(1L, OrganizationRole.MANAGER)));

    var ex = assertThrows(ReportPortalException.class,
        () -> handler.replace(remove(List.of(Map.of("id", 1, "org_role", "MEMBER"))), ORG_ID));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(organizationUserService, never()).deleteByOrganizationIdAndUserIdNotIn(ORG_ID, List.of(1L));
  }
}
