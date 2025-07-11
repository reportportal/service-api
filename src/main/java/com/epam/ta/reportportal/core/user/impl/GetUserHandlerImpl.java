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

package com.epam.ta.reportportal.core.user.impl;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EMAIL;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXPIRED;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_INSTANCE_USER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.InstanceUserPage;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.dao.GroupMembershipRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.group.GroupProject;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.UserResource;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

  private final UserCreationBidRepository userCreationBidRepository;

  private final GetJasperReportHandler<User> jasperReportHandler;

  /**
   * Constructor.
   *
   * @param userRepo                  User repository
   * @param projectRepository         Project repository
   * @param groupMembershipRepository Group project repository
   * @param userCreationBidRepository User creation bid repository
   * @param jasperReportHandler       Jasper report handler
   */
  @Autowired
  public GetUserHandlerImpl(
      UserRepository userRepo,
      ProjectRepository projectRepository,
      GroupMembershipRepository groupMembershipRepository,
      UserCreationBidRepository userCreationBidRepository,
      @Qualifier("userJasperReportHandler") GetJasperReportHandler<User> jasperReportHandler
  ) {
    this.userRepository = Preconditions.checkNotNull(userRepo);
    this.groupMembershipRepository = groupMembershipRepository;
    this.userCreationBidRepository = Preconditions.checkNotNull(userCreationBidRepository);
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
  public com.epam.ta.reportportal.model.Page<UserResource> getUsers(Filter filter, Pageable pageable,
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
  public YesNoRS validateInfo(String username, String email) {
    if (null != username) {
      Optional<User> user = userRepository.findByLogin(EntityUtils.normalizeId(username));
      return user.isPresent() ? new YesNoRS(true) : new YesNoRS(false);
    } else if (null != email) {
      Optional<User> user = userRepository.findByEmail(EntityUtils.normalizeId(email));
      return user.isPresent() ? new YesNoRS(true) : new YesNoRS(false);
    }
    return new YesNoRS(false);
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
  public com.epam.ta.reportportal.model.Page<UserResource> getAllUsers(Queryable filter, Pageable pageable) {
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

    final List<User> users = userRepository.findByFilter(filter);

    List<? extends Map<String, ?>> data = users.stream().map(jasperReportHandler::convertParams)
        .collect(Collectors.toList());

    JRDataSource jrDataSource = new JRBeanCollectionDataSource(data);

    //don't provide any params to not overwrite params from the Jasper template
    JasperPrint jasperPrint = jasperReportHandler.getJasperPrint(null, jrDataSource);

    jasperReportHandler.writeReport(reportFormat, outputStream, jasperPrint);
  }

  @Override
  public com.epam.ta.reportportal.model.Page<UserResource> searchUsers(String term, Pageable pageable) {

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

}
