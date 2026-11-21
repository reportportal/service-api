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

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Resolves a folder subtree with a count query first, so that a subtree over the limit
 * ({@code rp.tms.add-by-folder.max-test-cases}) is rejected before any ID is loaded.
 */
@Service
public class TmsFolderTestCaseResolverImpl implements TmsFolderTestCaseResolver {

  private static final String TEST_FOLDERS_NOT_FOUND = "TMS Test Folders with ids: %s for project: %d";
  private static final String TOO_MANY_TEST_CASES =
      "Folders contain %d test cases, the limit per request is %d";

  private final TmsTestFolderRepository tmsTestFolderRepository;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final int maxTestCases;

  public TmsFolderTestCaseResolverImpl(TmsTestFolderRepository tmsTestFolderRepository,
      TmsTestCaseRepository tmsTestCaseRepository,
      @Value("${rp.tms.add-by-folder.max-test-cases:5000}") int maxTestCases) {
    this.tmsTestFolderRepository = tmsTestFolderRepository;
    this.tmsTestCaseRepository = tmsTestCaseRepository;
    this.maxTestCases = maxTestCases;
  }

  @Override
  public List<Long> resolve(long projectId, List<Long> folderIds) {
    if (CollectionUtils.isEmpty(folderIds)) {
      return List.of();
    }
    var distinctFolderIds = folderIds.stream().distinct().toList();

    var existingFolderIds = new HashSet<>(
        tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(projectId, distinctFolderIds));
    var missingFolderIds = distinctFolderIds.stream()
        .filter(id -> !existingFolderIds.contains(id))
        .toList();
    if (!missingFolderIds.isEmpty()) {
      throw new ReportPortalException(NOT_FOUND,
          TEST_FOLDERS_NOT_FOUND.formatted(missingFolderIds, projectId));
    }

    var testCaseCount = tmsTestCaseRepository.countByProjectIdAndTestFolderIds(projectId,
        distinctFolderIds);
    if (testCaseCount > maxTestCases) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          TOO_MANY_TEST_CASES.formatted(testCaseCount, maxTestCases));
    }

    return tmsTestCaseRepository.findIdsByProjectIdAndTestFolderIds(projectId, distinctFolderIds);
  }
}
