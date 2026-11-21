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

package com.epam.reportportal.base.core.tms.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.sync.mapper.TmsSyncJobMapper;
import com.epam.reportportal.base.core.tms.sync.service.TmsFolderSyncService;
import com.epam.reportportal.base.core.tms.sync.service.TmsTestCaseSyncService;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class TmsSyncJobServiceImplTest {

  private static final long PROJECT_ID = 1L;
  private static final long INTEGRATION_ID = 2L;

  @Mock
  private TmsSyncJobRepository tmsSyncJobRepository;
  @Mock
  private IntegrationRepository integrationRepository;
  @Mock
  private TmsTestFolderRepository tmsTestFolderRepository;
  @Mock
  private TmsSyncJobMapper tmsSyncJobMapper;
  @Mock
  private TmsFolderSyncService tmsFolderSyncService;
  @Mock
  private TmsTestCaseSyncService tmsTestCaseSyncService;
  @Mock
  private PlatformTransactionManager transactionManager;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private TmsSyncJobServiceImpl sut;

  @BeforeEach
  void setUp() {
    sut = new TmsSyncJobServiceImpl(tmsSyncJobRepository, integrationRepository,
        tmsTestFolderRepository, List.of(), tmsSyncJobMapper, tmsFolderSyncService,
        tmsTestCaseSyncService, transactionManager, eventPublisher);
  }

  @Test
  void startSyncJob_LocalFolderOfAnotherProject_ThrowsNotFound() {
    when(integrationRepository.findByIdAndProjectIdForUpdate(INTEGRATION_ID, PROJECT_ID))
        .thenReturn(Optional.of(new Integration()));
    when(tmsTestFolderRepository.existsByIdAndProjectId(77L, PROJECT_ID)).thenReturn(false);

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.startSyncJob(PROJECT_ID, INTEGRATION_ID, "remote", 77L));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsSyncJobRepository, never()).save(any(TmsSyncJob.class));
  }
}
