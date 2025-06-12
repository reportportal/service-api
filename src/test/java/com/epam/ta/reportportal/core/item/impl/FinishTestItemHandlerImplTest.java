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

package com.epam.ta.reportportal.core.item.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.core.item.impl.status.ChangeStatusHandler;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.time.Instant;
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
class FinishTestItemHandlerImplTest {

  @Mock
  private TestItemRepository repository;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private IssueTypeHandler issueTypeHandler;

  @Mock
  private Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

  @Mock
  private StatusChangingStrategy statusChangingStrategy;

  @Mock
  private ChangeStatusHandler changeStatusHandler;

  @Mock
  private IssueEntityRepository issueEntityRepository;

  @Mock
  private MessageBus messageBus;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private FinishTestItemHandlerImpl handler;

  @Test
  void finishNotExistedTestItem() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    when(repository.findByUuid("1")).thenReturn(Optional.empty());
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.finishTestItem(rpUser, rpUserToMembership(rpUser), "1",
            new FinishTestItemRQ()
        )
    );
    assertEquals("Test Item '1' not found. Did you use correct Test Item ID?",
        exception.getMessage()
    );
  }

  @Test
  void finishTestItemUnderNotExistedLaunch() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    TestItem item = new TestItem();
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.IN_PROGRESS);
    item.setItemResults(results);
    item.setItemId(1L);
    when(repository.findByUuid("1")).thenReturn(Optional.of(item));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.finishTestItem(rpUser, rpUserToMembership(rpUser), "1",
            new FinishTestItemRQ()
        )
    );
    assertEquals("Launch '' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void finishTestItemByNotLaunchOwner() {
    final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER , 1L);
    TestItem item = new TestItem();
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setProjectId(1L);
    User user = new User();
    user.setId(2L);
    user.setLogin("owner");
    launch.setUserId(user.getId());
    item.setItemId(1L);
    item.setLaunchId(launch.getId());
    item.setHasChildren(false);
    when(repository.findByUuid("1")).thenReturn(Optional.of(item));
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.IN_PROGRESS);
    item.setItemResults(results);
    item.setItemId(1L);
    when(launchRepository.findById(any())).thenReturn(Optional.of(launch));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.finishTestItem(rpUser, rpUserToMembership(rpUser), "1",
            new FinishTestItemRQ()
        )
    );
    assertEquals("Finish test item is not allowed. You are not a launch owner.",
        exception.getMessage()
    );
  }

  @Test
  void finishStepItemWithoutProvidedStatus() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    TestItem item = new TestItem();
    item.setItemId(1L);
    item.setStartTime(Instant.now());
    item.setType(TestItemTypeEnum.SUITE);
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.IN_PROGRESS);
    item.setItemResults(results);
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUserId(1L);
    launch.setProjectId(1L);
    item.setLaunchId(launch.getId());
    item.setHasChildren(false);
    when(repository.findByUuid("1")).thenReturn(Optional.of(item));
    when(launchRepository.findById(any())).thenReturn(Optional.of(launch));
    doNothing().when(changeStatusHandler).changeLaunchStatus(any());
    doNothing().when(changeStatusHandler).changeParentStatus(any(), any(), any());

    final FinishTestItemRQ rq = new FinishTestItemRQ();
    rq.setEndTime(Instant.now());
    handler.finishTestItem(rpUser, rpUserToMembership(rpUser), "1",
        rq);
  }

  @Test
  void updateFinishedItemTest() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    TestItem item = new TestItem();
    item.setItemId(1L);
    TestItemResults results = new TestItemResults();
    results.setStatus(StatusEnum.PASSED);
    item.setItemResults(results);
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUserId(1L);
    launch.setProjectId(1L);
    item.setStartTime(Instant.now().minusSeconds(5L));
    item.setLaunchId(launch.getId());
    item.setType(TestItemTypeEnum.STEP);
    item.setHasStats(true);
    item.setHasChildren(false);
    when(launchRepository.findById(any())).thenReturn(Optional.of(launch));
    when(repository.findByUuid("1")).thenReturn(Optional.of(item));
    when(repository.findIdByUuidForUpdate(any())).thenReturn(Optional.of(item.getItemId()));
    when(repository.findById(item.getItemId())).thenReturn(Optional.of(item));

    IssueType issueType = new IssueType();
    issueType.setLocator("123");
    issueType.setIssueGroup(new IssueGroup());
    issueType.setLongName("123123");
    issueType.setHexColor("#1232asd");
    issueType.setShortName("short");

    when(issueTypeHandler.defineIssueType(any(), any())).thenReturn(issueType);
    when(statusChangingStrategyMapping.get(any(StatusEnum.class))).thenReturn(
        statusChangingStrategy);

    FinishTestItemRQ finishExecutionRQ = new FinishTestItemRQ();
    finishExecutionRQ.setStatus("FAILED");
    finishExecutionRQ.setEndTime(Instant.now());

    OperationCompletionRS operationCompletionRS =
        handler.finishTestItem(rpUser, rpUserToMembership(rpUser), "1",
            finishExecutionRQ
        );

    verify(statusChangingStrategy, times(1)).changeStatus(any(), any(), any(), eq(false));
    verify(issueEntityRepository, times(1)).save(any());
    verify(messageBus, times(1)).publishActivity(any());
    verify(eventPublisher, times(1)).publishEvent(any(IssueResolvedEvent.class));
  }
}
