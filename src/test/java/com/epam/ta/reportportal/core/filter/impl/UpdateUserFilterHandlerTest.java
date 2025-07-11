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

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.epam.ta.reportportal.core.filter.UpdateUserFilterHandler;
import com.epam.ta.reportportal.dao.GroupMembershipRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
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
  private UpdateUserFilterHandler updateUserFilterHandler =
      new UpdateUserFilterHandlerImpl(projectExtractor, userFilterRepository, messageBus);

  @Test
  void updateUserFilterWithTheSameName() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(SAME_NAME);

    MembershipDetails membershipDetails = rpUserToMembership(rpUser);
    when(userFilterRepository.findByIdAndProjectId(1L, membershipDetails.getProjectId())).thenReturn(
        Optional.of(userFilter));

    when(userFilter.getId()).thenReturn(1L);
    when(userFilter.getName()).thenReturn(SAME_NAME);
    when(userFilter.getProject()).thenReturn(project);
    when(project.getId()).thenReturn(1L);

    doNothing().when(messageBus).publishActivity(any(ActivityEvent.class));

    OperationCompletionRS operationCompletionRS =
        updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, membershipDetails, rpUser);

    assertEquals(
        "User filter with ID = '" + userFilter.getId() + "' successfully updated.",
        operationCompletionRS.getResultMessage()
    );
  }

  @Test
  void updateUserFilterWithAnotherNamePositive() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(ANOTHER_NAME);

    MembershipDetails membershipDetails = rpUserToMembership(rpUser);
    when(userFilterRepository.findByIdAndProjectId(1L, membershipDetails.getProjectId())).thenReturn(
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
        updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, membershipDetails, rpUser);

    assertEquals(
        "User filter with ID = '" + userFilter.getId() + "' successfully updated.",
        operationCompletionRS.getResultMessage()
    );
  }

  @Test
  void updateUserFilterWithAnotherNameNegative() {

    final ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  1L);

    UpdateUserFilterRQ updateUserFilterRQ = getUpdateRequest(ANOTHER_NAME);

    MembershipDetails membershipDetails = rpUserToMembership(rpUser);
    when(userFilterRepository.findByIdAndProjectId(1L, membershipDetails.getProjectId())).thenReturn(
        Optional.of(userFilter));

    when(userFilter.getId()).thenReturn(1L);
    when(userFilter.getName()).thenReturn(SAME_NAME);
    when(userFilter.getProject()).thenReturn(project);
    when(userFilter.getOwner()).thenReturn("user");
    when(project.getId()).thenReturn(1L);

    when(userFilterRepository.existsByNameAndOwnerAndProjectId(updateUserFilterRQ.getName(),
        userFilter.getOwner(), membershipDetails.getProjectId()
    )).thenReturn(Boolean.TRUE);

    doNothing().when(messageBus).publishActivity(any(ActivityEvent.class));

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> updateUserFilterHandler.updateUserFilter(1L, updateUserFilterRQ, membershipDetails,
            rpUser
        )
    );
    assertEquals(Suppliers.formattedSupplier(
        "User filter with name '{}' already exists for user '{}' under the project '{}'. You couldn't create the duplicate.",
        ANOTHER_NAME, "user", membershipDetails.getProjectName()
    ).get(), exception.getMessage());
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
