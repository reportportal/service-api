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

import static com.epam.reportportal.model.ValidationConstraints.MAX_PHOTO_HEIGHT;
import static com.epam.reportportal.model.ValidationConstraints.MAX_PHOTO_SIZE;
import static com.epam.reportportal.model.ValidationConstraints.MAX_PHOTO_WIDTH;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.fail;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.BINARY_DATA_CANNOT_BE_SAVED;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.entity.user.UserType.INTERNAL;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.ChangeUserTypeEvent;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.ImageFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.util.UserUtils;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Edit user handler
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@Service
public class EditUserHandlerImpl implements EditUserHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EditUserHandlerImpl.class);

  private final UserRepository userRepository;

  private final ProjectRepository projectRepository;

  private final UserBinaryDataService userBinaryDataService;

  private final PasswordEncoder passwordEncoder;

  private final AutoDetectParser autoDetectParser;

  private final MailServiceFactory emailServiceFactory;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public EditUserHandlerImpl(PasswordEncoder passwordEncoder, UserRepository userRepository,
      ProjectRepository projectRepository,
      UserBinaryDataService userBinaryDataService, AutoDetectParser autoDetectParser,
      MailServiceFactory emailServiceFactory, ApplicationEventPublisher eventPublisher) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.userBinaryDataService = userBinaryDataService;
    this.autoDetectParser = autoDetectParser;
    this.emailServiceFactory = emailServiceFactory;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public OperationCompletionRS editUser(String username, EditUserRQ editUserRq,
      ReportPortalUser editor) {
    User user = userRepository.findByLogin(username)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

    updateRestrictedFields(editor, user, editUserRq);

    if (null != editUserRq.getEmail() && !editUserRq.getEmail().equalsIgnoreCase(user.getEmail())) {
      String updEmail = editUserRq.getEmail().toLowerCase().trim();
      if (!editor.getUserRole().equals(UserRole.ADMINISTRATOR)) {
        expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED,
            "Unable to change email for external user");
      }
      expect(UserUtils.isEmailValid(updEmail), equalTo(true)).verify(BAD_REQUEST_ERROR,
          " wrong email: " + updEmail);
      final Optional<User> byEmail = userRepository.findByEmail(updEmail);

      expect(byEmail, Predicates.not(Optional::isPresent)).verify(USER_ALREADY_EXISTS, updEmail);

      List<Project> userProjects = projectRepository.findUserProjects(username);
      userProjects.forEach(
          project -> ProjectUtils.updateProjectRecipients(user.getEmail(), updEmail, project));
      user.setEmail(updEmail);
      try {
        projectRepository.saveAll(userProjects);
      } catch (Exception exp) {
        throw new ReportPortalException("PROJECT update exception while USER editing.", exp);
      }
    }

    if (null != editUserRq.getFullName()) {
      if (!editor.getUserRole().equals(UserRole.ADMINISTRATOR)) {
        expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED,
            "Unable to change full name for external user");
      }
      user.setFullName(editUserRq.getFullName());
    }

    ofNullable(editUserRq.getExternalId()).ifPresent(user::setExternalId);

    try {
      userRepository.save(user);
    } catch (Exception exp) {
      throw new ReportPortalException("Error while User editing.", exp);
    }

    return new OperationCompletionRS(
        "User with login = '" + user.getLogin() + "' successfully updated");
  }

  @Override
  public OperationCompletionRS uploadPhoto(String username, MultipartFile file) {
    User user = userRepository.findByLogin(username)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));
    validatePhoto(file);
    userBinaryDataService.saveUserPhoto(user, file);
    return new OperationCompletionRS("Profile photo has been uploaded successfully");
  }

  @Override
  public OperationCompletionRS uploadPhoto(Long userId, MultipartFile file) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));
    validatePhoto(file);
    userBinaryDataService.saveUserPhoto(user, file);
    return new OperationCompletionRS("Profile photo has been uploaded successfully");
  }

  @Override
  public OperationCompletionRS deletePhoto(String login) {
    User user = userRepository.findByLogin(login)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, login));
    expect(user.getUserType(), equalTo(INTERNAL)).verify(ACCESS_DENIED,
        "Unable to change photo for external user");
    userBinaryDataService.deleteUserPhoto(user);
    return new OperationCompletionRS("Profile photo has been deleted successfully");
  }

  @Override
  public OperationCompletionRS changePassword(ReportPortalUser loggedInUser,
      ChangePasswordRQ request) {
    User user = userRepository.findByLogin(loggedInUser.getUsername())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));

    expect(user.getUserType(), equalTo(INTERNAL)).verify(FORBIDDEN_OPERATION,
        "Impossible to change password for external users.");
    expect(passwordEncoder.matches(request.getOldPassword(), user.getPassword()),
        Predicate.isEqual(true)).verify(FORBIDDEN_OPERATION,
        "Old password not match with stored."
    );
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    try {
      emailServiceFactory.getDefaultEmailService(true)
          .sendChangePasswordConfirmation("Change password confirmation",
              new String[]{loggedInUser.getEmail()},
              loggedInUser.getUsername()
          );
    } catch (Exception e) {
      LOGGER.warn("Unable to send email.", e);
    }

    return new OperationCompletionRS("Password has been changed successfully");
  }

  private void updateRestrictedFields(ReportPortalUser editor, User user, EditUserRQ editUserRq) {
    ofNullable(editUserRq.getRole()).ifPresent(role -> {
      checkPossibilityToEdit(editor, user, "role");
      UserRole newRole = UserRole.findByName(role)
          .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
              "Incorrect specified Account Role parameter."));
      publishChangeUserTypeEvent(user, editor, newRole);
      user.setRole(newRole);
    });
    ofNullable(editUserRq.getActive()).ifPresent(isActive -> {
      checkPossibilityToEdit(editor, user, "active");
      user.setActive(isActive);
    });
    ofNullable(editUserRq.getAccountType()).ifPresent(accountType -> {
      checkPossibilityToEdit(editor, user, "accountType");
      user.setUserType(UserType.valueOf(accountType));
    });
  }

  private void validatePhoto(MultipartFile file) {
    expect(file.getSize() < MAX_PHOTO_SIZE, equalTo(true)).verify(BINARY_DATA_CANNOT_BE_SAVED,
        "Image size should be less than 1 mb");

    final MediaType mediaType = resolveMediaType(file);
    try (final InputStream inputStream = file.getInputStream()) {
      Dimension dimension = getImageDimension(mediaType, inputStream).orElseThrow(
          () -> new ReportPortalException(
              BINARY_DATA_CANNOT_BE_SAVED,
              "Unable to resolve image size"
          ));
      expect(
          (dimension.getHeight() <= MAX_PHOTO_HEIGHT) && (dimension.getWidth() <= MAX_PHOTO_WIDTH),
          equalTo(true)).verify(
          BINARY_DATA_CANNOT_BE_SAVED,
          "Image size should be 300x500px or less"
      );
    } catch (IOException e) {
      fail().withError(BINARY_DATA_CANNOT_BE_SAVED);
    }
  }

  private MediaType resolveMediaType(MultipartFile file) {
    return ofNullable(file.getContentType()).flatMap(
            string -> ofNullable(MediaType.parse(string)).filter(mediaType -> ImageFormat.fromValue(
                mediaType.getSubtype()).isPresent()))
        .orElseGet(() -> {
          try (final TikaInputStream tikaInputStream = TikaInputStream.get(file.getInputStream())) {
            MediaType mediaType = autoDetectParser.getDetector()
                .detect(tikaInputStream, new Metadata());
            expect(ImageFormat.fromValue(mediaType.getSubtype()), Optional::isPresent).verify(
                BINARY_DATA_CANNOT_BE_SAVED,
                "Image format should be " + ImageFormat.getValues()
            );
            return mediaType;
          } catch (IOException e) {
            throw new ReportPortalException(BINARY_DATA_CANNOT_BE_SAVED);
          }
        });
  }

  private Optional<Dimension> getImageDimension(MediaType mediaType, InputStream inputStream) {
    for (Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(
        String.valueOf(mediaType)); iterator.hasNext(); ) {
      ImageReader reader = iterator.next();
      try (ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
        reader.setInput(stream);
        int width = reader.getWidth(reader.getMinIndex());
        int height = reader.getHeight(reader.getMinIndex());
        return Optional.of(new Dimension(width, height));
      } catch (IOException e) {
        //Try next ImageReader
      } finally {
        reader.dispose();
      }
    }
    return Optional.empty();
  }

  private void publishChangeUserTypeEvent(User user, ReportPortalUser editor, UserRole newRole) {
    eventPublisher.publishEvent(
        new ChangeUserTypeEvent(user.getId(), user.getLogin(),
            user.getRole(), newRole, editor.getUserId(), editor.getUsername()));
  }

  private void checkPossibilityToEdit(ReportPortalUser editor, User user, String fieldName) {
    BusinessRule.expect(editor.getUserRole(), equalTo(UserRole.ADMINISTRATOR))
        .verify(ACCESS_DENIED, "Current Account Role can't update " + fieldName);
    BusinessRule.expect(user, u -> !u.getLogin().equalsIgnoreCase(editor.getUsername()))
        .verify(ErrorType.ACCESS_DENIED, "You cannot update your " + fieldName);
  }
}
