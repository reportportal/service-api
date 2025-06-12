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

package com.epam.ta.reportportal.core.filter.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.GroupMembershipRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.filter.Order;
import com.epam.ta.reportportal.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.model.filter.UserFilterCondition;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class UpdateUserFilterHandlerTest {

  public static final String SAME_NAME = "name";
  public static final String ANOTHER_NAME = "another name";

  private UserFilter userFilter = mock(UserFilter.class);

  private Project project = mock(Project.class);

  private ProjectUserRepository projectUserRepository = mock(ProjectUserRepository.class);
  private GroupMembershipRepository groupMembershipRepository = mock(
      GroupMembershipRepository.class);

  private ProjectExtractor projectExtractor = new ProjectExtractor(
      projectUserRepository,
      groupMembershipRepository
  );
  private UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
  private MessageBus messageBus = mock(MessageBus.class);
  private UpdateUserFilterHandlerImpl updateUserFilterHandler =
      new UpdateUserFilterHandlerImpl(projectExtractor, userFilterRepository, messageBus);

  @Test
  void updateUserFilterWithTheSameName() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(SAME_NAME);

    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, "test_project");
    when(userFilterRepository.findByIdAndProjectId(1L, projectDetails.getProjectId())).thenReturn(
        Optional.of(userFilter));

    when(userFilter.getId()).thenReturn(1L);
    when(userFilter.getName()).thenReturn(SAME_NAME);
    when(userFilter.getProject()).thenReturn(project);
    when(project.getId()).thenReturn(1L);

    doNothing().when(messageBus).publishActivity(any(ActivityEvent.class));

    OperationCompletionRS operationCompletionRS =
        updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, projectDetails, rpUser);

    assertEquals(
        "User filter with ID = '" + userFilter.getId() + "' successfully updated.",
        operationCompletionRS.getResultMessage()
    );
  }

  @Test
  void updateUserFilterWithAnotherNamePositive() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(ANOTHER_NAME);

    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, "test_project");
    when(userFilterRepository.findByIdAndProjectId(1L, projectDetails.getProjectId())).thenReturn(
        Optional.of(userFilter));

    when(userFilter.getId()).thenReturn(1L);
    when(userFilter.getName()).thenReturn(SAME_NAME);
    when(userFilter.getProject()).thenReturn(project);
    when(project.getId()).thenReturn(1L);

    when(
        userFilterRepository.existsByNameAndProjectId(updateUserFilterRQ.getName(), 1L)).thenReturn(
        Boolean.FALSE);

    doNothing().when(messageBus).publishActivity(any(ActivityEvent.class));

    OperationCompletionRS operationCompletionRS =
        updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, projectDetails, rpUser);

    assertEquals(
        "User filter with ID = '" + userFilter.getId() + "' successfully updated.",
        operationCompletionRS.getResultMessage()
    );
  }

  @Test
  void updateUserFilterWithAnotherNameNegative() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(ANOTHER_NAME);

    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, "test_project");
    when(userFilterRepository.findByIdAndProjectId(1L, projectDetails.getProjectId())).thenReturn(
        Optional.of(userFilter));

    when(userFilter.getId()).thenReturn(1L);
    when(userFilter.getName()).thenReturn(SAME_NAME);
    when(userFilter.getProject()).thenReturn(project);
    when(userFilter.getOwner()).thenReturn("user");
    when(project.getId()).thenReturn(1L);

    when(userFilterRepository.existsByNameAndProjectId(updateUserFilterRQ.getName(),
        projectDetails.getProjectId())).thenReturn(Boolean.TRUE);

    doNothing().when(messageBus).publishActivity(any(ActivityEvent.class));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, projectDetails,
            rpUser
        )
    );
    assertEquals(Suppliers.formattedSupplier(
        "User filter with name '{}' already exists for user '{}' under the project '{}'. You couldn't create the duplicate.",
        ANOTHER_NAME, "user", projectDetails.getProjectName()
    ).get(), exception.getMessage());
  }

  @Test
  void validateFilterName_shouldAddCopySuffix_ifNameExistsOnce() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("existing_filter");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("existing_filter", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("existing_filter_copy", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertThat(request.getName()).isEqualTo("existing_filter_copy");
  }

  @Test
  void validateFilterName_shouldAddCopyWithIncrementingNumber_ifNameExistsMultipleTimes() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("popular_filter");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("popular_filter", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("popular_filter_copy", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("popular_filter_copy_1", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("popular_filter_copy_2", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertThat(request.getName()).isEqualTo("popular_filter_copy_2");
  }

  @Test
  void validateFilterName_shouldHandleExistingCopySuffixCorrectly() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("filter_with_copy_in_middle");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("filter_with_copy_in_middle", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("filter_with_copy_in_middle_copy", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertThat(request.getName()).isEqualTo("filter_with_copy_in_middle_copy");
  }

  @Test
  void validateFilterName_shouldNotAddCopySufix() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("filter_copy");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("filter_copy", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("filter_copy_copy_1", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertNotEquals("filter_copy_copy", request.getName());
  }

  @Test
  void validateFilterName_shouldAddCount() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("filter_copy_10");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("filter_copy_10", projectId))
        .thenReturn(false);
    when(userFilterRepository.existsByNameAndProjectId("filter_copy_copy_1", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertEquals("filter_copy_11", request.getName());
  }

  @Test
  void validateFilterName_shouldAddCopySufixTest() {
    // Given
    UpdateUserFilterRQ request = new UpdateUserFilterRQ();
    request.setName("filter_copy");
    Long projectId = 1L;

    when(userFilterRepository.existsByNameAndProjectId("filter_copy", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("filter_copy_1", projectId))
        .thenReturn(true);
    when(userFilterRepository.existsByNameAndProjectId("filter_copy_2", projectId))
        .thenReturn(false);

    // When
    updateUserFilterHandler.validateFilterName(request, projectId);

    // Then
    assertThat(request.getName()).isEqualTo("filter_copy_2");
  }

  private UpdateUserFilterRQ getUpdateRequest(String name) {

    UpdateUserFilterRQ updateUserFilterRQ = new UpdateUserFilterRQ();

    updateUserFilterRQ.setName(name);
    updateUserFilterRQ.setObjectType("Launch");

    Order order = new Order();
    order.setIsAsc(true);
    order.setSortingColumnName(CRITERIA_NAME);
    updateUserFilterRQ.setOrders(Lists.newArrayList(order));

    UserFilterCondition condition = new UserFilterCondition(CRITERIA_NAME, "cnt", "we");
    updateUserFilterRQ.setConditions(Sets.newHashSet(condition));

    return updateUserFilterRQ;
  }

}