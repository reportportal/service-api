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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsFolderTestCaseResolverImplTest {

  private static final long PROJECT_ID = 1L;
  private static final int MAX_TEST_CASES = 5;

  @Mock
  private TmsTestFolderRepository tmsTestFolderRepository;

  @Mock
  private TmsTestCaseRepository tmsTestCaseRepository;

  private TmsFolderTestCaseResolverImpl sut;

  @BeforeEach
  void setUp() {
    sut = new TmsFolderTestCaseResolverImpl(tmsTestFolderRepository, tmsTestCaseRepository,
        MAX_TEST_CASES);
  }

  @Test
  void shouldReturnEmptyListWithoutQueriesWhenNoFolders() {
    assertTrue(sut.resolve(PROJECT_ID, null).isEmpty());
    assertTrue(sut.resolve(PROJECT_ID, List.of()).isEmpty());

    verifyNoInteractions(tmsTestFolderRepository, tmsTestCaseRepository);
  }

  @Test
  void shouldResolveDistinctFoldersToTestCaseIds() {
    when(tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(PROJECT_ID, List.of(7L, 8L)))
        .thenReturn(List.of(7L, 8L));
    when(tmsTestCaseRepository.countByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L, 8L)))
        .thenReturn(3L);
    when(tmsTestCaseRepository.findIdsByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L, 8L)))
        .thenReturn(List.of(10L, 20L, 30L));

    var result = sut.resolve(PROJECT_ID, List.of(7L, 8L, 7L));

    assertEquals(List.of(10L, 20L, 30L), result);
  }

  @Test
  void shouldAcceptExactlyTheLimit() {
    when(tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(PROJECT_ID, List.of(7L)))
        .thenReturn(List.of(7L));
    when(tmsTestCaseRepository.countByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L)))
        .thenReturn((long) MAX_TEST_CASES);
    when(tmsTestCaseRepository.findIdsByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L)))
        .thenReturn(List.of(1L, 2L, 3L, 4L, 5L));

    assertEquals(MAX_TEST_CASES, sut.resolve(PROJECT_ID, List.of(7L)).size());
  }

  @Test
  void shouldThrowNotFoundForMissingOrForeignFolder() {
    when(tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(PROJECT_ID, List.of(7L, 99L)))
        .thenReturn(List.of(7L));

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.resolve(PROJECT_ID, List.of(7L, 99L)));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    assertTrue(exception.getMessage().contains("[99]"));
    verifyNoInteractions(tmsTestCaseRepository);
  }

  @Test
  void shouldThrowBadRequestOverLimitBeforeLoadingIds() {
    when(tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(PROJECT_ID, List.of(7L)))
        .thenReturn(List.of(7L));
    when(tmsTestCaseRepository.countByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L)))
        .thenReturn(MAX_TEST_CASES + 1L);

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.resolve(PROJECT_ID, List.of(7L)));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Folders contain 6 test cases, the limit per request is 5"));
    verify(tmsTestCaseRepository, never()).findIdsByProjectIdAndTestFolderIds(anyLong(), any());
  }

  @Test
  void shouldThrowBadRequestWhenLoadedIdsExceedLimitAfterCount() {
    when(tmsTestFolderRepository.findExistingIdsByProjectIdAndIdIn(PROJECT_ID, List.of(7L)))
        .thenReturn(List.of(7L));
    when(tmsTestCaseRepository.countByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L)))
        .thenReturn((long) MAX_TEST_CASES);
    when(tmsTestCaseRepository.findIdsByProjectIdAndTestFolderIds(PROJECT_ID, List.of(7L)))
        .thenReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L));

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.resolve(PROJECT_ID, List.of(7L)));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    assertTrue(exception.getMessage().contains("Folders contain 6 test cases, the limit per request is 5"));
  }
}
