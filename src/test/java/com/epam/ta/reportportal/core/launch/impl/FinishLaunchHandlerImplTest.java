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

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.hierarchy.FinishHierarchyHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.BulkRQ;
import com.epam.ta.reportportal.model.launch.FinishLaunchRS;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class FinishLaunchHandlerImplTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private FinishHierarchyHandler<Launch> finishHierarchyHandler;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private MessageBus messageBus;

  @Mock
  private ApplicationEventPublisher publisher;

  @InjectMocks
  private FinishLaunchHandlerImpl handler;

  @InjectMocks
  private StopLaunchHandlerImpl stopLaunchHandler;

  @Test
  void finishLaunch() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findByUuid("1")).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    FinishLaunchRS response =
        handler.finishLaunch("1", finishExecutionRQ, rpUserToMembership(rpUser),
            rpUser, null
        );

    verify(finishHierarchyHandler, times(1)).finishDescendants(any(), any(), any(), any(), any());

    assertNotNull(response);
  }

  @Test
  void finishLaunchWithLink() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findByUuid("1")).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    final FinishLaunchRS finishLaunchRS =
        handler.finishLaunch("1", finishExecutionRQ, rpUserToMembership(rpUser),
            rpUser, "http://example.com"
        );

    verify(finishHierarchyHandler, times(1)).finishDescendants(any(), any(), any(), any(), any());

    assertNotNull(finishLaunchRS);
    assertEquals("http://example.com/ui/#o-slug.project-name/launches/all/1", finishLaunchRS.getLink());
  }

  @Test
  void stopLaunch() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    final OperationCompletionRS response = stopLaunchHandler.stopLaunch(1L, finishExecutionRQ,
        rpUserToMembership(rpUser), rpUser,
        "http://example.com"
    );
    assertNotNull(response);
    assertEquals("Launch with ID = '1' successfully stopped.", response.getResultMessage());
  }

  @Test
  void bulkStopLaunch() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    Map<Long, FinishExecutionRQ> entities = new HashMap<>();
    entities.put(1L, finishExecutionRQ);

    BulkRQ<Long, FinishExecutionRQ> bulkRq = new BulkRQ<>();
    bulkRq.setEntities(entities);

    ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findById(1L)).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    final List<OperationCompletionRS> response = stopLaunchHandler.stopLaunch(bulkRq,
        rpUserToMembership(rpUser),
        rpUser,
        "http://example.com"
    );
    assertNotNull(response);
    assertEquals(1, response.size());
  }

  @Test
  void finishWithIncorrectStatus() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    final ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findByUuid("1")).thenReturn(
        getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

    assertThrows(ReportPortalException.class, () -> handler.finishLaunch("1", finishExecutionRQ,
        rpUserToMembership(rpUser), rpUser, null
    ));
  }

  @Test
  void finishWithIncorrectEndTime() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now().minus(5, ChronoUnit.HOURS));

    final ReportPortalUser rpUser =
        getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    when(launchRepository.findByUuid("1")).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    assertThrows(ReportPortalException.class, () -> handler.finishLaunch("1", finishExecutionRQ,
        rpUserToMembership(rpUser), rpUser, null
    ));
  }

  @Test
  void finishNotOwnLaunch() {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(Instant.now());

    final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER,  1L);
    rpUser.setUserId(2L);

    when(launchRepository.findByUuid("1")).thenReturn(
        getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.finishLaunch("1", finishExecutionRQ,
            rpUserToMembership(rpUser), rpUser, null
        )
    );
    assertEquals("You do not have enough permissions. You are not launch owner.",
        exception.getMessage()
    );
  }
}
