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
import static com.epam.ta.reportportal.core.item.impl.UpdateTestItemHandlerImpl.INITIAL_STATUS_ATTRIBUTE_KEY;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.item.impl.status.StatusChangingStrategy;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.model.item.UpdateTestItemRQ;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class UpdateTestItemHandlerImplTest {

  private final StatusChangingStrategy statusChangingStrategy = mock(StatusChangingStrategy.class);

  @Mock
  private Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

  @Mock
  private TestItemRepository itemRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private TestItemService testItemService;

  @Mock
  private MessageBus messageBus;

  @InjectMocks
  private UpdateTestItemHandlerImpl handler;

  @Test
  void updateNotExistedTestItem() {
    final ReportPortalUser rpUser =
        getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  1L);
    when(itemRepository.findById(1L)).thenReturn(Optional.empty());
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateTestItem(rpUserToMembership(rpUser), 1L,
            new UpdateTestItemRQ(), rpUser
        )
    );
    assertEquals("Test Item '1' not found. Did you use correct Test Item ID?",
        exception.getMessage()
    );
  }

  @Test
  void updateTestItemUnderNotExistedLaunch() {
    final ReportPortalUser rpUser =
        getRpUser("test", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  1L);

    TestItem testItem = new TestItem();
    testItem.setLaunchId(2L);
    when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(testItemService.getEffectiveLaunch(testItem)).thenThrow(
        new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateTestItem(rpUserToMembership(rpUser), 1L,
            new UpdateTestItemRQ(), rpUser
        )
    );
    assertEquals("Launch '' not found. Did you use correct Launch ID?", exception.getMessage());
  }

  @Test
  void updateTestItemUnderNotOwnLaunch() {
    final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER , 1L);

    TestItem item = new TestItem();
    Launch launch = new Launch();
    launch.setId(1L);
    User user = new User();
    user.setId(1L);
    user.setLogin("owner");
    launch.setUserId(2L);
    launch.setProjectId(1L);
    item.setLaunchId(launch.getId());
    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateTestItem(rpUserToMembership(rpUser), 1L,
            new UpdateTestItemRQ(), rpUser
        )
    );
    assertEquals("You do not have enough permissions. You are not a launch owner.",
        exception.getMessage()
    );
  }

  @Test
  void updateTestItemFromAnotherProject() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);
    TestItem item = new TestItem();
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUserId(1L);
    launch.setProjectId(2L);
    item.setLaunchId(launch.getId());
    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateTestItem(rpUserToMembership(rpUser), 1L,
            new UpdateTestItemRQ(), rpUser
        )
    );
    assertEquals("You do not have enough permissions. Launch is not under the specified project.",
        exception.getMessage()
    );
  }

  @Test
  void defineIssuesOnNotExistProject() {
    ReportPortalUser rpUser = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    when(projectRepository.findById(1L)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.defineTestItemsIssues(rpUserToMembership(rpUser),
            new DefineIssueRQ(), rpUser
        )
    );

    assertEquals("Project '1' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void changeNotStepItemStatus() {
    ReportPortalUser user = getRpUser("user", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);

    UpdateTestItemRQ rq = new UpdateTestItemRQ();
    rq.setStatus("FAILED");

    long itemId = 1L;
    TestItem item = new TestItem();
    item.setItemId(itemId);
    item.setHasChildren(true);
    item.setType(TestItemTypeEnum.TEST);
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.PASSED);
    item.setItemResults(itemResults);
    Launch launch = new Launch();
    launch.setId(2L);
    item.setLaunchId(launch.getId());

    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateTestItem(extractProjectDetails(user, TEST_PROJECT_KEY), itemId, rq, user)
    );
    assertEquals("Incorrect Request. Unable to change status on test item with children",
        exception.getMessage()
    );
  }

  @Test
  void shouldCreateInitialStatusAttribute() {
    ReportPortalUser user =
        getRpUser("user", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    UpdateTestItemRQ rq = new UpdateTestItemRQ();
    rq.setStatus("PASSED");

    long itemId = 1L;
    TestItem item = new TestItem();
    item.setItemId(itemId);
    item.setHasChildren(false);
    item.setType(TestItemTypeEnum.STEP);
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.FAILED);
    item.setItemResults(itemResults);
    Launch launch = new Launch();
    launch.setId(2L);
    item.setLaunchId(launch.getId());

    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
    doNothing().when(messageBus).publishActivity(any());
    when(statusChangingStrategyMapping.get(StatusEnum.PASSED)).thenReturn(statusChangingStrategy);
    doNothing().when(statusChangingStrategy).changeStatus(item, StatusEnum.PASSED, user, true);

    handler.updateTestItem(extractProjectDetails(user, TEST_PROJECT_KEY), itemId, rq, user);
    assertTrue(item.getAttributes().stream().anyMatch(
        attribute -> INITIAL_STATUS_ATTRIBUTE_KEY.equalsIgnoreCase(attribute.getKey())
            && StatusEnum.FAILED.getExecutionCounterField().equalsIgnoreCase("failed")));
  }

  @Test
  void shouldNotCreateInitialStatusAttribute() {
    ReportPortalUser user =
        getRpUser("user", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    UpdateTestItemRQ rq = new UpdateTestItemRQ();
    rq.setStatus("PASSED");

    long itemId = 1L;
    TestItem item = new TestItem();
    item.setItemId(itemId);
    item.setHasChildren(false);
    item.setType(TestItemTypeEnum.STEP);
    item.setAttributes(
        Sets.newHashSet(new ItemAttribute(INITIAL_STATUS_ATTRIBUTE_KEY, "passed", true)));
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.FAILED);
    item.setItemResults(itemResults);
    Launch launch = new Launch();
    launch.setId(2L);
    item.setLaunchId(launch.getId());

    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
    doNothing().when(messageBus).publishActivity(any());
    when(statusChangingStrategyMapping.get(StatusEnum.PASSED)).thenReturn(statusChangingStrategy);
    doNothing().when(statusChangingStrategy).changeStatus(item, StatusEnum.PASSED, user, true);

    handler.updateTestItem(extractProjectDetails(user, TEST_PROJECT_KEY), itemId, rq, user);
    assertTrue(item.getAttributes().stream().anyMatch(
        attribute -> INITIAL_STATUS_ATTRIBUTE_KEY.equalsIgnoreCase(attribute.getKey())
            && StatusEnum.PASSED.getExecutionCounterField().equalsIgnoreCase("passed")));
  }

  @Test
  void updateItemPositive() {
    ReportPortalUser user =
        getRpUser("user", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);

    UpdateTestItemRQ rq = new UpdateTestItemRQ();
    rq.setDescription("new description");

    long itemId = 1L;
    TestItem item = new TestItem();
    item.setItemId(itemId);
    item.setDescription("old description");
    item.setHasChildren(false);
    item.setType(TestItemTypeEnum.STEP);
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.FAILED);
    item.setItemResults(itemResults);
    Launch launch = new Launch();
    launch.setId(2L);
    item.setLaunchId(launch.getId());

    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

    OperationCompletionRS response =
        handler.updateTestItem(extractProjectDetails(user, TEST_PROJECT_KEY), itemId, rq, user);

    assertEquals("TestItem with ID = '1' successfully updated.", response.getResultMessage());
    assertEquals(rq.getDescription(), item.getDescription());
  }
}
