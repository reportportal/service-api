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

package com.epam.reportportal.core.user.impl;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EMAIL;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXPIRED;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER;
import static com.epam.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.reportportal.ws.converter.converters.UserConverter.TO_INSTANCE_USER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.InstanceUserPage;
import com.epam.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.reportportal.core.jasper.ReportFormat;
import com.epam.reportportal.core.user.GetUserHandler;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.dao.GroupMembershipRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.group.GroupProject;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectUtils;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.user.UserResource;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.reportportal.ws.converter.converters.UserConverter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for GET user operations.
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class GetUserHandlerImpl implements GetUserHandler {

  private final UserRepository userRepository;

  private final ProjectRepository projectRepository;

  private final GroupMembershipRepository groupMembershipRepository;

  private final GetJasperReportHandler<User> jasperReportHandler;

  /**
   * Constructor.
   *
   * @param userRepo                  User repository
   * @param projectRepository         Project repository
   * @param groupMembershipRepository Group project repository
   * @param jasperReportHandler       Jasper report handler
   */
  @Autowired
  public GetUserHandlerImpl(
      UserRepository userRepo,
      ProjectRepository projectRepository,
      GroupMembershipRepository groupMembershipRepository,
      @Qualifier("userJasperReportHandler") GetJasperReportHandler<User> jasperReportHandler
  ) {
    this.userRepository = Preconditions.checkNotNull(userRepo);
    this.groupMembershipRepository = groupMembershipRepository;
    this.projectRepository = projectRepository;
    this.jasperReportHandler = jasperReportHandler;
  }

  @Override
  public UserResource getUser(String username, ReportPortalUser loggedInUser) {

    User user = userRepository.findByLogin(username.toLowerCase())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
    return UserConverter.TO_RESOURCE.apply(user);
  }

  @Override
  public UserResource getUser(ReportPortalUser loggedInUser) {
    User user = userRepository.findByLogin(loggedInUser.getUsername())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
    List<GroupProject> groupProjects = groupMembershipRepository.findAllUserProjects(user.getId());
    return UserConverter.TO_RESOURCE_WITH_GROUPS.apply(user, groupProjects);
  }

  @Override
  public InstanceUser getCurrentUser(ReportPortalUser loggedInUser) {
    Filter filterById = new Filter(User.class, Lists.newArrayList());
    filterById.withCondition(
        new FilterCondition(Condition.EQUALS, false, loggedInUser.getUserId().toString(), CRITERIA_ID));
    User user = userRepository.findByFilter(filterById).stream()
        .findFirst()
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
    return UserConverter.TO_INSTANCE_USER.apply(user);

  }

  @Override
  public com.epam.reportportal.model.Page<UserResource> getUsers(Filter filter, Pageable pageable,
      MembershipDetails membershipDetails) {
    // Active users only
    filter.withCondition(new FilterCondition(Condition.EQUALS, false, "false", CRITERIA_EXPIRED));
    filter.withCondition(new FilterCondition(Condition.EQUALS,
        false,
        String.valueOf(membershipDetails.getProjectId()),
        CRITERIA_PROJECT_ID
    ));

    return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE)
        .apply(userRepository.findByFilterExcluding(filter, pageable, "email"));
  }


  @Override
  public Map<String, UserResource.AssignedProject> getUserProjects(String userName) {
    return projectRepository.findUserProjects(userName).stream()
        .collect(toMap(Project::getName, it -> {
          UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
          ProjectUser projectUser = ProjectUtils.findUserConfigByLogin(it, userName);

          ofNullable(ofNullable(projectUser).orElseThrow(
                  () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userName))
              .getProjectRole()).ifPresent(role -> assignedProject.setProjectRole(role.name()));

          return assignedProject;
        }));
  }

  @Override
  public com.epam.reportportal.model.Page<UserResource> getAllUsers(Queryable filter, Pageable pageable) {
    final Page<User> users = userRepository.findByFilter(filter, pageable);
    return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE).apply(users);
  }

  @Override
  public InstanceUserPage getUsersExcluding(Queryable filter, Pageable pageable,
      String... excludeFields) {
    final Page<User> users = userRepository.findByFilterExcluding(filter, pageable, excludeFields);

    var items = users.getContent().stream()
        .map(TO_INSTANCE_USER)
        .toList();
    InstanceUserPage instanceUserPage = new InstanceUserPage()
        .items(items);

    return responseWithPageParameters(instanceUserPage, pageable, users.getTotalElements());
  }

  @Override
  public void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter) {
    exportUsers(reportFormat, outputStream, filter, null);
  }

  @Override
  public void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter, Pageable pageable) {
    var users = (pageable == null)
        ? userRepository.findByFilter(filter)
        : userRepository.findByFilter(filter, pageable);

    List<? extends Map<String, ?>> data = StreamSupport.stream(users.spliterator(), false)
        .map(jasperReportHandler::convertParams)
        .collect(Collectors.toList());
    var jrDataSource = new JRBeanCollectionDataSource(data);
    //don't provide any params to not overwrite params from the Jasper template
    var jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);
    var bytes = jasperReportHandler.exportReportBytes(reportFormat, jasperPrint);
    try {
      outputStream.write(bytes);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response."
      );
    }
  }

  @Override
  public com.epam.reportportal.model.Page<UserResource> searchUsers(String term, Pageable pageable) {

    Filter filter = Filter.builder()
        .withTarget(User.class)
        .withCondition(
            new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_USER))
        .withCondition(
            new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_EMAIL))
        .build();
    return PagedResourcesAssembler.pageConverter(UserConverter.TO_RESOURCE)
        .apply(userRepository.findByFilter(filter, pageable));

  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, User> getUserMap(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return userRepository.findByIds(userIds)
        .stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserById(Long userId) {
    if (userId == null) {
      return null;
    }

    return userRepository
        .findById(userId)
        .orElse(null);
  }
}
