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

package com.epam.reportportal.base.core.launch.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.core.remover.ContentRemover;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.AttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteLaunchHandlerImplTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private AttachmentRepository attachmentRepository;

  @Mock
  private ContentRemover<Launch> launchContentRemover;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private LogIndexer logIndexer;

  @Mock
  private LogService logService;

  @InjectMocks
  private DeleteLaunchHandlerImpl handler;

  @Test
  void deleteLaunchMovesAttachmentsBeforeDeletingLaunch() {
    final ReportPortalUser rpUser = getRpUser("owner", UserRole.ADMINISTRATOR,
        OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);
    Launch launch = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

    handler.deleteLaunch(1L, rpUserToMembership(rpUser), rpUser);

    InOrder inOrder = inOrder(attachmentRepository, launchRepository);
    inOrder.verify(attachmentRepository).moveForDeletionByLaunchId(1L);
    inOrder.verify(launchRepository).delete(launch);
  }

  @Test
  void deleteLaunchesMovesAttachmentsBeforeDeletingLaunches() {
    final ReportPortalUser rpUser = getRpUser("owner", UserRole.ADMINISTRATOR,
        OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);
    Launch launch = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

    handler.deleteLaunches(List.of(1L), rpUserToMembership(rpUser), rpUser);

    InOrder inOrder = inOrder(attachmentRepository, launchRepository);
    inOrder.verify(attachmentRepository).moveForDeletionByLaunchIds(List.of(1L));
    inOrder.verify(launchRepository).deleteAll(List.of(launch));
  }

  @Test
  void deleteNotOwnLaunch() {
    final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER,
        1L);
    rpUser.setUserId(2L);
    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.deleteLaunch(1L, rpUserToMembership(rpUser), rpUser)
    );
    assertEquals("You do not have enough permissions. You are not launch owner.",
        exception.getMessage());
  }

  @Test
  void deleteLaunchFromAnotherProject() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 2L);
    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.deleteLaunch(1L, rpUserToMembership(rpUser), rpUser)
    );
    assertEquals("Forbidden operation. Target launch '1' not under specified project '2'",
        exception.getMessage());
  }

  @Test
  void deleteLaunchInProgressStatus() {

    final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);
    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    assertThrows(ReportPortalException.class,
        () -> handler.deleteLaunch(1L, rpUserToMembership(rpUser), rpUser));
  }

}
