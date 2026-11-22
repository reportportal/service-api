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

package com.epam.reportportal.base.core.group;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupOrganizationBindingTest {

  private final ProjectRepository projectRepository = mock(ProjectRepository.class);
  private final OrganizationUserRepository organizationUserRepository = mock(OrganizationUserRepository.class);
  private final GroupExtensionPoint extension = mock(GroupExtensionPoint.class);
  private final GroupOrganizationBinding binding =
      new GroupOrganizationBinding(projectRepository, organizationUserRepository);

  @BeforeEach
  void setUp() {
    when(extension.getGroupById(5L)).thenReturn(Optional.of(new GroupInfo().orgId(1L)));
  }

  private static Project project(Long id, Long orgId) {
    var project = new Project();
    project.setId(id);
    project.setOrganizationId(orgId);
    return project;
  }

  @Test
  void projectFromSameOrganizationIsAccepted() {
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L, 1L)));
    assertDoesNotThrow(() -> binding.requireProjectInGroupOrganization(extension, 5L, 10L));
  }

  @Test
  void projectFromOtherOrganizationIsRejected() {
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L, 2L)));
    var ex = assertThrows(ReportPortalException.class,
        () -> binding.requireProjectInGroupOrganization(extension, 5L, 10L));
    assertEquals(ErrorType.BAD_REQUEST_ERROR, ex.getErrorType());
  }

  @Test
  void projectByKeyFromOtherOrganizationIsRejected() {
    when(projectRepository.findByKey("p")).thenReturn(Optional.of(project(10L, 2L)));
    assertThrows(ReportPortalException.class, () -> binding.requireProjectInGroupOrganization(extension, 5L, "p"));
  }

  @Test
  void unknownGroupIsNotFound() {
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L, 1L)));
    when(extension.getGroupById(6L)).thenReturn(Optional.empty());
    var ex = assertThrows(ReportPortalException.class,
        () -> binding.requireProjectInGroupOrganization(extension, 6L, 10L));
    assertEquals(ErrorType.NOT_FOUND, ex.getErrorType());
  }

  @Test
  void groupWithoutOrganizationIsRejected() {
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L, 1L)));
    when(extension.getGroupById(7L)).thenReturn(Optional.of(new GroupInfo()));
    assertThrows(ReportPortalException.class, () -> binding.requireProjectInGroupOrganization(extension, 7L, 10L));
  }

  @Test
  void userOutsideOrganizationIsRejected() {
    when(organizationUserRepository.findByUserIdAndOrganization_Id(9L, 1L)).thenReturn(Optional.empty());
    assertThrows(ReportPortalException.class, () -> binding.requireUserInGroupOrganization(extension, 5L, 9L));
  }

  @Test
  void organizationMemberIsAccepted() {
    when(organizationUserRepository.findByUserIdAndOrganization_Id(9L, 1L))
        .thenReturn(Optional.of(new OrganizationUser()));
    assertDoesNotThrow(() -> binding.requireUserInGroupOrganization(extension, 5L, 9L));
  }
}
