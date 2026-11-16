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
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for replicating external OAuth2/SAML identity provider users into the ReportPortal user store.
 *
 * @author Andrei Varabyeu
 */
@AllArgsConstructor
@Slf4j
public class AbstractUserReplicator {

  private static final String EMAIL_NOT_PROVIDED_MSG = "Email not provided";

  protected final UserRepository userRepository;
  protected final ContentTypeResolver contentTypeResolver;
  protected final UserBinaryDataService userBinaryDataService;

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

  /**
   * Normalizes email for login and enforces that it is present.
   *
   * @param email raw email from the IdP; must be non-blank
   * @return normalized lower-case email
   */
  protected String validateEmail(String email) {
    if (isNullOrEmpty(email)) {
      throw new UserSynchronizationException(EMAIL_NOT_PROVIDED_MSG);
    }
    return email.toLowerCase();
  }

  /**
   * Stores the user's avatar from prepared binary.
   *
   * @param user the user whose photo is updated
   * @param data binary payload and content metadata
   */
  protected void uploadPhoto(User user, BinaryData data) {
    userBinaryDataService.saveUserPhoto(user, data);
  }

  /**
   * Sniffs content type and stores the photo from raw bytes.
   *
   * @param user the user whose photo is updated
   * @param data raw image or avatar bytes
   */
  protected void uploadPhoto(User user, byte[] data) {
    uploadPhoto(user, new BinaryData(resolveContentType(data), (long) data.length,
        new ByteArrayInputStream(data)));
  }

  private String resolveContentType(byte[] data) {
    return contentTypeResolver.detectContentType(data);
  }

}
