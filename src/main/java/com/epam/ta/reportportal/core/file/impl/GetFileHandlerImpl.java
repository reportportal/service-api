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
package com.epam.ta.reportportal.core.file.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.binary.UserBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetFileHandlerImpl implements GetFileHandler {

  private static final String DEFAULT_USER_PHOTO = "image/defaultAvatar.png";

  private final UserRepository userRepository;

  private final UserBinaryDataService userDataStoreService;

  private final AttachmentBinaryDataService attachmentBinaryDataService;

  private final ProjectExtractor projectExtractor;

  @Override
  public BinaryData getUserPhoto(ReportPortalUser loggedInUser, boolean loadThumbnail) {
    User user = userRepository.findByLogin(loggedInUser.getUsername()).orElseThrow(
        () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
    return userDataStoreService.loadUserPhoto(user, loadThumbnail);
  }

  @Override
  public BinaryData getUserPhoto(Long userId, boolean loadThumbnail) {
    var user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, userId));

    return userDataStoreService.loadUserPhoto(user, loadThumbnail);
  }


  @Override
  public BinaryData getUserPhoto(String username, ReportPortalUser loggedInUser, String projectKey,
      boolean loadThumbnail) {
    Optional<User> userOptional = userRepository.findByLogin(username);
    if (userOptional.isEmpty()) {
      log.warn("User '{}' not found", username);
      return getDefaultPhoto();
    }
    User user = userOptional.get();
    MembershipDetails membershipDetails = projectExtractor.extractProjectDetailsAdmin(projectKey);
    if (loggedInUser.getUserRole() != UserRole.ADMINISTRATOR) {
      expect(ProjectUtils.isAssignedToProject(user, membershipDetails.getProjectId()),
          Predicate.isEqual(true)).verify(ErrorType.ACCESS_DENIED,
          formattedSupplier("You are not assigned to project '{}'",
              membershipDetails.getProjectName()));
    }
    return userDataStoreService.loadUserPhoto(user, loadThumbnail);
  }

  @Override
  public BinaryData loadFileById(Long fileId, MembershipDetails membershipDetails) {
    return attachmentBinaryDataService.load(fileId, membershipDetails);
  }

  private BinaryData getDefaultPhoto() {
    try {
      var data = new ClassPathResource(DEFAULT_USER_PHOTO).getInputStream();
      var contentType = MimeTypeUtils.IMAGE_JPEG_VALUE;
      return new BinaryData(contentType, (long) data.available(), data);
    } catch (IOException e) {
      log.error("Unable to load default photo", e);
      throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR,
          "Unable to load default photo");
    }
  }
}
