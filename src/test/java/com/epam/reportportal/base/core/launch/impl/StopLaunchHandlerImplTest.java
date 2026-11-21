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

package com.epam.reportportal.base.core.launch.impl;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.launch.changes.LaunchChangesHandler;
import com.epam.reportportal.base.core.statistics.TestItemStatisticsService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class StopLaunchHandlerImplTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private TestItemStatisticsService testItemStatisticsService;

  @Mock
  private LaunchChangesHandler launchChangesHandler;

  @InjectMocks
  private StopLaunchHandlerImpl handler;

  @Test
  void takesAdvisoryLockBeforeLaunchRowIsSaved() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setStatus(StatusEnum.IN_PROGRESS);
    launch.setStartTime(Instant.now().minusSeconds(60));
    when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

    ReportPortalUser user = mock(ReportPortalUser.class);
    when(user.getUserRole()).thenReturn(UserRole.ADMINISTRATOR);
    when(user.getUserId()).thenReturn(1L);
    MembershipDetails membership = mock(MembershipDetails.class);

    FinishExecutionRQ rq = new FinishExecutionRQ();
    rq.setEndTime(Instant.now());

    handler.stopLaunch(1L, rq, membership, user, "http://base");

    InOrder order = inOrder(testItemStatisticsService, launchRepository, testItemRepository);
    order.verify(testItemStatisticsService).acquireAdvisoryLock(1L);
    order.verify(launchRepository).save(launch);
    order.verify(testItemRepository).interruptInProgressItems(1L);
    order.verify(testItemStatisticsService).addInterruptionStatistics(1L);
  }
}
