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

import static com.epam.ta.reportportal.ws.converter.converters.ExceptionConverter.TO_ERROR_RS;

import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.events.activity.UnassignUserEvent;
import com.epam.ta.reportportal.core.events.activity.UserDeletedEvent;
import com.epam.ta.reportportal.core.events.activity.UsersDeletedEvent;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.core.project.settings.notification.ProjectRecipientHandler;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.model.activity.UserActivityResource;
import com.epam.ta.reportportal.util.email.strategy.EmailNotificationStrategy;
import com.epam.ta.reportportal.util.email.strategy.EmailTemplate;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delete user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
@Transactional
public class DeleteUserHandlerImpl implements DeleteUserHandler {

  private final UserBinaryDataService dataStore;

  private final UserRepository userRepository;

  private final DeleteProjectHandler deleteProjectHandler;

  private final ContentRemover<User> userContentRemover;

  private final ProjectRecipientHandler projectRecipientHandler;

  private final ProjectRepository projectRepository;

  private final Map<EmailTemplate, EmailNotificationStrategy> emailNotificationStrategyMapping;

  private final ApplicationEventPublisher applicationEventPublisher;

  private static final String DELETED_USER = "deleted_user";

  @Value("${rp.environment.variable.allow-delete-account:false}")
  private boolean isAllowToDeleteAccount;

  @Autowired
  public DeleteUserHandlerImpl(UserRepository userRepository,
      DeleteProjectHandler deleteProjectHandler, ContentRemover<User> userContentRemover,
      UserBinaryDataService dataStore, ProjectRecipientHandler projectRecipientHandler,
      ProjectRepository projectRepository,
      Map<EmailTemplate, EmailNotificationStrategy> emailNotificationStrategyMapping,
      ApplicationEventPublisher applicationEventPublisher) {
    this.userRepository = userRepository;
    this.deleteProjectHandler = deleteProjectHandler;
    this.dataStore = dataStore;
    this.userContentRemover = userContentRemover;
    this.projectRecipientHandler = projectRecipientHandler;
    this.projectRepository = projectRepository;
    this.emailNotificationStrategyMapping = emailNotificationStrategyMapping;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  @Transactional
  public OperationCompletionRS deleteUser(Long userId, ReportPortalUser loggedInUser) {
    User deletedUser = deleteUserWithAssociatedData(userId, loggedInUser);

    publishUserDeletedEvent(deletedUser, loggedInUser);

    return new OperationCompletionRS("User with ID = '" + userId + "' successfully deleted.");
  }

  private User deleteUserWithAssociatedData(Long userId, ReportPortalUser loggedInUser) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));
    validateUserDeletion(userId, loggedInUser);

    userContentRemover.remove(user);

    List<Project> userProjects = projectRepository.findAllByUserLogin(user.getLogin());
    userProjects.forEach(project -> {
      if (ProjectUtils.isPersonalForUser(project.getProjectType(), project.getName(),
          user.getLogin()
      )) {
        deleteProjectHandler.deleteProject(project.getId(), null);
      } else {
        projectRecipientHandler.handle(Lists.newArrayList(user), project);
        publishUserUnassignEvent(user, loggedInUser, project.getId());
      }
    });

    dataStore.deleteUserPhoto(user);
    userRepository.delete(user);
    sendEmailAboutDeletion(user, loggedInUser);

    return user;
  }

  private void sendEmailAboutDeletion(User user, ReportPortalUser loggedInUser) {
    EmailTemplate template = user.getId().equals(loggedInUser.getUserId())
        ? EmailTemplate.USER_SELF_DELETION_NOTIFICATION
        : EmailTemplate.USER_DELETION_NOTIFICATION;
    emailNotificationStrategyMapping.get(template)
        .sendEmail(user.getEmail(), Collections.emptyMap());
  }

  @Override
  @Transactional
  public DeleteBulkRS deleteUsers(List<Long> ids, ReportPortalUser currentUser) {
    List<ReportPortalException> exceptions = Lists.newArrayList();
    List<Long> deleted = Lists.newArrayList();
    ids.forEach(userId -> {
      try {
        deleteUserWithAssociatedData(userId, currentUser);
        deleted.add(userId);
      } catch (ReportPortalException rp) {
        exceptions.add(rp);
      }
    });
    publishUsersDeletedEvent(deleted.size(), currentUser);
    return new DeleteBulkRS(deleted, Collections.emptyList(),
        exceptions.stream().map(TO_ERROR_RS).collect(Collectors.toList())
    );
  }

  private void validateUserDeletion(Long userId, ReportPortalUser loggedInUser) {
    BusinessRule.expect(
            UserRole.ADMINISTRATOR.equals(loggedInUser.getUserRole()) && Objects.equals(userId,
                loggedInUser.getUserId()
            ), Predicates.equalTo(false))
        .verify(ErrorType.ACCESS_DENIED, "You cannot delete own account");

    BusinessRule.expect(
            UserRole.ADMINISTRATOR.equals(loggedInUser.getUserRole()) || (isAllowToDeleteAccount
                && loggedInUser.getUserId().equals(userId)), Predicates.equalTo(true))
        .verify(ErrorType.ACCESS_DENIED, "You are not allowed to delete account");
  }

  private void publishUserDeletedEvent(User deletedUser, ReportPortalUser loggedInUser) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(deletedUser.getId());
    userActivityResource.setFullName(DELETED_USER);

    if (loggedInUser.getUserId().equals(deletedUser.getId())) {
      applicationEventPublisher.publishEvent(
          new UserDeletedEvent(userActivityResource, loggedInUser.getUserId(), DELETED_USER));
    } else {
      applicationEventPublisher.publishEvent(
          new UserDeletedEvent(userActivityResource, loggedInUser.getUserId(),
              loggedInUser.getUsername()
          ));
    }
  }

  private void publishUsersDeletedEvent(int deletedCount, ReportPortalUser loggedInUser) {
    UserActivityResource userActivityResource = new UserActivityResource();
    if (deletedCount == 1) {
      userActivityResource.setFullName(deletedCount + " deleted user");
    } else {
      userActivityResource.setFullName(deletedCount + " deleted users");
    }

    applicationEventPublisher.publishEvent(
        new UsersDeletedEvent(userActivityResource, loggedInUser.getUserId(),
            loggedInUser.getUsername()
        ));
  }

  private void publishUserUnassignEvent(User deletedUser, ReportPortalUser authorizedUser,
      Long projectId) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(deletedUser.getId());
    userActivityResource.setFullName(DELETED_USER);
    userActivityResource.setDefaultProjectId(projectId);

    applicationEventPublisher.publishEvent(new UnassignUserEvent(userActivityResource));
  }
}
