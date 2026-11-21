/*
 * Copyright 2026 EPAM Systems
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
package com.epam.reportportal.base.core.tms.service;

import java.util.List;

/**
 * Resolves folder IDs to the IDs of the test cases in their whole subtrees, for adding a folder to a
 * test plan or a manual launch.
 */
public interface TmsFolderTestCaseResolver {

  /**
   * Returns the distinct IDs of the test cases in the given folders and all their subfolders.
   *
   * @param projectId project ID
   * @param folderIds root folder IDs; {@code null} or empty resolves to an empty list
   * @return test case IDs ordered by ID
   * @throws com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException
   *     NOT_FOUND if a folder is not in the project; BAD_REQUEST_ERROR if the folders contain more
   *     test cases than the configured limit
   */
  List<Long> resolve(long projectId, List<Long> folderIds);
}
