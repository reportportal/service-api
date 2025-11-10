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

package com.epam.reportportal.infrastructure.persistence.binary.impl;


import static com.epam.reportportal.infrastructure.persistence.binary.impl.DataStoreUtils.ATTACHMENT_CONTENT_TYPE;
import static com.epam.reportportal.infrastructure.persistence.binary.impl.DataStoreUtils.PHOTOS_PATH;
import static com.epam.reportportal.infrastructure.persistence.binary.impl.DataStoreUtils.ROOT_USER_PHOTO_DIR;
import static com.epam.reportportal.infrastructure.persistence.binary.impl.DataStoreUtils.USER_DATA_PATH;
import static com.epam.reportportal.infrastructure.persistence.binary.impl.DataStoreUtils.buildThumbnailFileName;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.infrastructure.persistence.binary.UserBinaryDataService;
import com.epam.reportportal.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class UserBinaryDataServiceImpl implements UserBinaryDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserBinaryDataServiceImpl.class);
  private static final String DEFAULT_USER_PHOTO = "image/defaultAvatar.png";

  private final DataStoreService dataStoreService;

  private final FeatureFlagHandler featureFlagHandler;

  @Autowired
  public UserBinaryDataServiceImpl(
      @Qualifier("userDataStoreService") DataStoreService dataStoreService,
      FeatureFlagHandler featureFlagHandler) {
    this.dataStoreService = dataStoreService;
    this.featureFlagHandler = featureFlagHandler;
  }

  @Override
  public void saveUserPhoto(User user, MultipartFile file) {
    try {
      saveUserPhoto(user, file.getInputStream(), file.getContentType());
    } catch (IOException e) {
      LOGGER.error("Unable to save user photo", e);
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED, e);
    }
  }

  @Override
  public void saveUserPhoto(User user, BinaryData binaryData) {
    saveUserPhoto(user, binaryData.getInputStream(), binaryData.getContentType());
  }

  @Override
  public void saveUserPhoto(User user, InputStream inputStream, String contentType) {
    try {
      byte[] data = StreamUtils.copyToByteArray(inputStream);
      try (InputStream userPhotoCopy = new ByteArrayInputStream(data);
          InputStream thumbnailCopy = new ByteArrayInputStream(data)) {
        if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
          user.setAttachment(dataStoreService.save(
              Paths.get(USER_DATA_PATH, PHOTOS_PATH, user.getLogin()).toString(), userPhotoCopy));
          user.setAttachmentThumbnail(dataStoreService.saveThumbnail(
              buildThumbnailFileName(Paths.get(USER_DATA_PATH, PHOTOS_PATH).toString(),
                  user.getLogin()
              ), thumbnailCopy));
        } else {
          user.setAttachment(
              dataStoreService.save(Paths.get(ROOT_USER_PHOTO_DIR, user.getLogin()).toString(),
                  userPhotoCopy
              ));
          user.setAttachmentThumbnail(dataStoreService.saveThumbnail(
              buildThumbnailFileName(ROOT_USER_PHOTO_DIR, user.getLogin()), thumbnailCopy));
        }
      }
      ofNullable(user.getMetadata()).orElseGet(() -> new Metadata(Maps.newHashMap())).getMetadata()
          .put(ATTACHMENT_CONTENT_TYPE, contentType);
    } catch (IOException e) {
      LOGGER.error("Unable to save user photo", e);
    }
  }

  @Override
  public BinaryData loadUserPhoto(User user, boolean loadThumbnail) {
    Optional<String> fileId =
        ofNullable(loadThumbnail ? user.getAttachmentThumbnail() : user.getAttachment());
    InputStream data;
    String contentType;
    try {
      if (fileId.isPresent()) {
        contentType = (String) user.getMetadata().getMetadata().get(ATTACHMENT_CONTENT_TYPE);
        data = dataStoreService.load(fileId.get()).orElseThrow(
            () -> new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, fileId.get()));
      } else {
        data = new ClassPathResource(DEFAULT_USER_PHOTO).getInputStream();
        contentType = MimeTypeUtils.IMAGE_JPEG_VALUE;
      }
      return new BinaryData(contentType, (long) data.available(), data);
    } catch (IOException e) {
      LOGGER.error("Unable to load user photo", e);
      throw new ReportPortalException(
          ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Unable to load user photo");
    }
  }

  @Override
  public void deleteUserPhoto(User user) {
    ofNullable(user.getAttachment()).ifPresent(fileId -> {
      dataStoreService.delete(fileId);
      user.setAttachment(null);
      Optional.ofNullable(user.getAttachmentThumbnail()).ifPresent(thumbnailId -> {
        dataStoreService.delete(thumbnailId);
        user.setAttachmentThumbnail(null);
      });
      ofNullable(user.getMetadata()).ifPresent(
          metadata -> metadata.getMetadata().remove(ATTACHMENT_CONTENT_TYPE));
    });
  }
}
