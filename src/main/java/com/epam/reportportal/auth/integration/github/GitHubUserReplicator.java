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

package com.epam.reportportal.auth.integration.github;

import static com.epam.reportportal.auth.util.AuthUtils.NORMALIZE_STRING;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.epam.reportportal.auth.event.UserEventPublisher;
import com.epam.reportportal.auth.integration.AbstractUserReplicator;
import com.epam.reportportal.auth.oauth.UserSynchronizationException;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replicates GitHub account info with internal ReportPortal's database.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class GitHubUserReplicator extends AbstractUserReplicator {

  /**
   * GitHub user replicator constructor.
   */
  public GitHubUserReplicator(UserRepository userRepository, ProjectRepository projectRepository,
      PersonalProjectService personalProjectService, UserBinaryDataService userBinaryDataService,
      ContentTypeResolver contentTypeResolver, UserEventPublisher userEventPublisher) {
    super(userRepository, projectRepository, personalProjectService, userBinaryDataService,
        contentTypeResolver);
    this.userEventPublisher = userEventPublisher;
  }

  private final UserEventPublisher userEventPublisher;

  /**
   * Synchronizes user with GitHub account.
   *
   * @param accessToken GitHub access token
   */
  public void synchronizeUser(String accessToken) {
    GitHubClient gitHubClient = GitHubClient.withAccessToken(accessToken);
    UserResource userResource = gitHubClient.getUser();

    var email = resolveEmail(userResource, gitHubClient);
    var user = userRepository.findByEmail(email)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userResource.getEmail()));

    if (!user.getUserType().equals(UserType.GITHUB)) {
      throw new ReportPortalException(
          ErrorType.INCORRECT_AUTHENTICATION_TYPE,
          "User '" + userResource.getEmail() + "' is not GitHUB user");
    }

    updateUser(user, userResource, gitHubClient);

    userRepository.save(user);
  }

  /**
   * Replicates GitHub user to internal database (if does NOT exist). Updates if exist. Creates personal project for
   * that user
   *
   * @param userResource GitHub user to be replicated
   * @param gitHubClient Configured GitHub client
   * @return Internal User representation
   */
  @Transactional
  public ReportPortalUser replicateUser(UserResource userResource, GitHubClient gitHubClient) {
    String email = resolveEmail(userResource, gitHubClient);

    User user = userRepository.findByEmail(email).map(u -> {
      if (UserType.GITHUB.equals(u.getUserType())) {
        updateUser(u, userResource, gitHubClient);
      } else {
        throw new UserSynchronizationException(
            "User with login '" + u.getLogin() + "' already exists");
      }
      return u;
    }).orElseGet(() -> {
      var created = createUser(userResource, gitHubClient);
      var saved = userRepository.save(created);
      userEventPublisher.publishOnUserCreated(saved);
      return saved;
    });

    return ReportPortalUser.userBuilder().fromUser(user);
  }

  private void updateUser(User user, UserResource userResource, GitHubClient gitHubClient) {
    user.setFullName(
        isNullOrEmpty(userResource.getName()) ? user.getLogin() : userResource.getName());
    user.setMetadata(defaultMetaData());
    uploadAvatar(gitHubClient, user, userResource.getAvatarUrl());
  }

  private User createUser(UserResource userResource, GitHubClient gitHubClient) {
    User user = new User();
    var email = resolveEmail(userResource, gitHubClient);
    user.setLogin(email);
    user.setEmail(email);
    user.setUuid(UUID.randomUUID());
    user.setActive(Boolean.TRUE);

    updateUser(user, userResource, gitHubClient);
    user.setUserType(UserType.GITHUB);
    user.setRole(UserRole.USER);
    user.setExpired(false);
    return user;
  }

  private void uploadAvatar(GitHubClient gitHubClient, User user, String avatarUrl) {
    if (null != avatarUrl) {
      ResponseEntity<Resource> photoRs = gitHubClient.downloadResource(avatarUrl);
      try (InputStream photoStream = Objects.requireNonNull(photoRs.getBody()).getInputStream()) {
        BinaryData photo = new BinaryData(
            Objects.requireNonNull(photoRs.getHeaders().getContentType()).toString(),
            photoRs.getBody().contentLength(),
            photoStream
        );
        uploadPhoto(user, photo);
      } catch (IOException e) {
        LOGGER.error("Unable to load photo for user {}", user.getLogin());
      }
    }
  }

  /**
   * Retrieves all emails from GitHub API including non-public ones. It is assumed that the user has only one verified
   * email address.
   *
   * @param gitHubClient GitHub client
   * @return Optional email address
   */
  private Optional<String> retrieveEmail(GitHubClient gitHubClient) {
    return gitHubClient.getUserEmails()
        .stream()
        .filter(EmailResource::isVerified)
        .filter(EmailResource::isPrimary)
        .findFirst()
        .map(EmailResource::getEmail);
  }

  private String resolveEmail(UserResource user, GitHubClient client) {
    return Optional.ofNullable(user.getEmail())
        .filter(StringUtils::isNotBlank)
        .map(NORMALIZE_STRING)
        .orElseGet(() -> retrieveEmail(client)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(
                () -> new UserSynchronizationException("User 'email' has not been provided")));
  }
}
