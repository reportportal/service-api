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

package com.epam.reportportal.auth.integration;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.oauth.UserSynchronizationException;
import com.epam.reportportal.base.infrastructure.commons.ContentTypeResolver;
import com.epam.reportportal.base.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrei Varabyeu
 */
public class AbstractUserReplicator {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserReplicator.class);
  private static final String EMAIL_NOT_PROVIDED_MSG = "Email not provided";

  protected final UserRepository userRepository;
  protected final ProjectRepository projectRepository;
  protected final PersonalProjectService personalProjectService;
  protected UserBinaryDataService userBinaryDataService;
  private final ContentTypeResolver contentTypeResolver;

  public AbstractUserReplicator(UserRepository userRepository, ProjectRepository projectRepository,
      PersonalProjectService personalProjectService, UserBinaryDataService userBinaryDataService,
      ContentTypeResolver contentTypeResolver) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.personalProjectService = personalProjectService;
    this.userBinaryDataService = userBinaryDataService;
    this.contentTypeResolver = contentTypeResolver;
  }

  /**
   * Generates personal project if it does NOT exist.
   *
   * @param user Owner of personal project
   * @return Created project name
   */
  protected Project generatePersonalProject(User user) {
    return projectRepository.findByName(personalProjectService.getProjectPrefix(user.getLogin()))
        .orElse(generatePersonalProjectByUser(user));
  }

  /**
   * Generates default meta info.
   *
   * @return Default meta info
   */
  protected Metadata defaultMetaData() {
    Map<String, Object> metaDataMap = new HashMap<>();
    long nowInMillis = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    metaDataMap.put("last_login", nowInMillis);
    metaDataMap.put("synchronizationDate", nowInMillis);
    return new Metadata(metaDataMap);
  }

  /**
   * Updates last syncronization data for specified user.
   *
   * @param user User to be synchronized
   */
  protected void updateSynchronizationDate(User user) {
    Metadata metadata = ofNullable(user.getMetadata()).orElse(
        new Metadata(
            Maps.newHashMap()));
    metadata.getMetadata()
        .put("synchronizationDate", LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
    user.setMetadata(metadata);
  }

  /**
   * Checks email is available.
   *
   * @param email email to check
   */
  protected void checkExistingEmail(String email) {
    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      throw new UserSynchronizationException("User with email '" + email + "' already exists");
    }
  }

  protected String validateEmail(String email) {
    if (isNullOrEmpty(email)) {
      throw new UserSynchronizationException(EMAIL_NOT_PROVIDED_MSG);
    }
    return email.toLowerCase();
  }

  protected void uploadPhoto(User user, BinaryData data) {
    userBinaryDataService.saveUserPhoto(user, data);
  }

  protected void uploadPhoto(User user, byte[] data) {
    uploadPhoto(user, new BinaryData(resolveContentType(data), (long) data.length,
        new ByteArrayInputStream(data)));
  }

  private String resolveContentType(byte[] data) {
    return contentTypeResolver.detectContentType(data);
  }

  private Project generatePersonalProjectByUser(User user) {
    Project personalProject = personalProjectService.generatePersonalProject(user);
    return projectRepository.save(personalProject);
  }
}
