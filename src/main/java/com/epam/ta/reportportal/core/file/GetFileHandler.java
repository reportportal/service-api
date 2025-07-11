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
package com.epam.ta.reportportal.core.file;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import java.io.InputStream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface GetFileHandler {

  /**
   * Returns {@link InputStream} for current logged-in user photo
   *
   * @param loggedInUser  Logged-in {@link ReportPortalUser}
   * @param loadThumbnail true if need to load thumbnail
   * @return {@link InputStream}
   */
  BinaryData getUserPhoto(ReportPortalUser loggedInUser, boolean loadThumbnail);

  /**
   * Returns {@link InputStream} for current logged-in user photo.
   *
   * @param userId        requested user Id {@link Long}
   * @param loadThumbnail true if needed to load thumbnail
   * @return {@link BinaryData}
   */
  BinaryData getUserPhoto(Long userId, boolean loadThumbnail);

  /**
   * Returns {@link InputStream} for photo of the {@link com.epam.ta.reportportal.entity.user.User}
   * with specified username
   *
   * @param username       Username of user which photo to get
   * @param loggedInUser   Logged-in {@link ReportPortalUser}
   * @param projectName    Project name
   * @param loadThumbnail  true if need to load thumbnail
   * @return {@link InputStream}
   */
  BinaryData getUserPhoto(String username, ReportPortalUser loggedInUser, String projectName,
      boolean loadThumbnail);

  /**
   * Returns {@link BinaryData} for the file with the specified id
   *
   * @param fileId Id of the file to get
   * @param membershipDetails {@link MembershipDetails}
   * @return {@link BinaryData} file data
   */
  BinaryData loadFileById(Long fileId, MembershipDetails membershipDetails);
}
