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
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetFileHandlerImpl implements GetFileHandler {

  private final UserRepository userRepository;

  private final UserBinaryDataService userDataStoreService;

  private final AttachmentBinaryDataService attachmentBinaryDataService;

  private final ProjectExtractor projectExtractor;

  @Override
  public BinaryData getUserPhoto(ReportPortalUser loggedInUser, boolean loadThumbnail) {
    User user = userRepository.findByLogin(loggedInUser.getUsername())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, loggedInUser.getUsername()));
    return userDataStoreService.loadUserPhoto(user, loadThumbnail);
  }

  @Override
  public BinaryData getUserPhoto(String username, ReportPortalUser loggedInUser, String projectName,
      boolean loadThumbnail) {
    Optional<User> userOptional = userRepository.findByLogin(username);
    if (userOptional.isEmpty()) {
      log.warn("User '{}' not found", username);
      return new BinaryData("", 0L, null);
    }
    User user = userOptional.get();
    ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetailsAdmin(
        loggedInUser, projectName);
    if (loggedInUser.getUserRole() != UserRole.ADMINISTRATOR) {
      expect(
          ProjectUtils.isAssignedToProject(user, projectDetails.getProjectId()),
          Predicate.isEqual(true)
      ).verify(ErrorType.ACCESS_DENIED, formattedSupplier("You are not assigned to project '{}'",
          projectDetails.getProjectName()));
    }
    return userDataStoreService.loadUserPhoto(user, loadThumbnail);
  }

  @Override
  public BinaryData loadFileById(Long fileId, ReportPortalUser.ProjectDetails projectDetails) {
    return attachmentBinaryDataService.load(fileId, projectDetails);
  }
}
