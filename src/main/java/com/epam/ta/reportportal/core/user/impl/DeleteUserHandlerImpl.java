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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.DeleteBulkRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteUserHandlerImpl.class);

  private final UserBinaryDataService dataStore;

  private final UserRepository userRepository;

  private final DeleteProjectHandler deleteProjectHandler;

  private final ContentRemover<User> userContentRemover;

  private final ProjectRecipientHandler projectRecipientHandler;

  private final ProjectRepository projectRepository;

  private final MailServiceFactory emailServiceFactory;

  @Value("${rp.environment.variable.allow-delete-account:false}")
  private boolean isAllowToDeleteAccount;

  @Autowired
  public DeleteUserHandlerImpl(UserRepository userRepository,
      DeleteProjectHandler deleteProjectHandler,
      ContentRemover<User> userContentRemover, UserBinaryDataService dataStore,
      ProjectRecipientHandler projectRecipientHandler, ProjectRepository projectRepository,
      MailServiceFactory emailServiceFactory) {
    this.userRepository = userRepository;
    this.deleteProjectHandler = deleteProjectHandler;
    this.dataStore = dataStore;
    this.userContentRemover = userContentRemover;
    this.projectRecipientHandler = projectRecipientHandler;
    this.projectRepository = projectRepository;
    this.emailServiceFactory = emailServiceFactory;
  }

  @Override
  @Transactional
  public OperationCompletionRS deleteUser(Long userId, ReportPortalUser loggedInUser) {
    deleteUserWithAssociatedData(userId, loggedInUser);

    sendEmailAboutDeletion(loggedInUser);

    return new OperationCompletionRS("User with ID = '" + userId + "' successfully deleted.");
  }

  private void deleteUserWithAssociatedData(Long userId, ReportPortalUser loggedInUser) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));
    validateUserDeletion(userId, loggedInUser);

    userContentRemover.remove(user);

    List<Project> userProjects = projectRepository.findAllByUserLogin(user.getLogin());
    userProjects.forEach(project -> {
      if (ProjectUtils.isPersonalForUser(project.getProjectType(), project.getName(),
          user.getLogin())) {
        deleteProjectHandler.deleteProject(project.getId());
      } else {
        projectRecipientHandler.handle(Lists.newArrayList(user), project);
      }
    });

    dataStore.deleteUserPhoto(user);
    userRepository.delete(user);
  }

  private void sendEmailAboutDeletion(ReportPortalUser loggedInUser) {
    try {
      EmailService defaultEmailService = emailServiceFactory.getDefaultEmailService(true);
      defaultEmailService.sendDeletionNotification(loggedInUser.getEmail());
    } catch (Exception e) {
      LOGGER.warn("Unable to send email.", e);
    }
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
    return new DeleteBulkRS(deleted, Collections.emptyList(),
        exceptions.stream().map(TO_ERROR_RS).collect(Collectors.toList())
    );
  }

  private void validateUserDeletion(Long userId, ReportPortalUser loggedInUser) {
    BusinessRule.expect(
            UserRole.ADMINISTRATOR.equals(loggedInUser.getUserRole()) && Objects.equals(userId,
                loggedInUser.getUserId()), Predicates.equalTo(false))
        .verify(ErrorType.INCORRECT_REQUEST, "You cannot delete own account");

    BusinessRule.expect(
            UserRole.ADMINISTRATOR.equals(loggedInUser.getUserRole()) || (isAllowToDeleteAccount
                && loggedInUser.getUserId().equals(userId)), Predicates.equalTo(true))
        .verify(ErrorType.INCORRECT_REQUEST, "You are not allowed to delete account");
  }
}
