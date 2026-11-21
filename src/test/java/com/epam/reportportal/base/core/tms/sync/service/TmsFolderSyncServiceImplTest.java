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

package com.epam.reportportal.base.core.tms.sync.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncScopeConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsFolderSyncServiceImplTest {

  private static final long PROJECT_ID = 1L;

  @Mock
  private TmsTestFolderRepository tmsTestFolderRepository;
  @Mock
  private TmsTestFolderMapper tmsTestFolderMapper;
  @Mock
  private TmsSyncJobRepository tmsSyncJobRepository;

  @InjectMocks
  private TmsFolderSyncServiceImpl sut;

  private TmsSyncJob job;

  @BeforeEach
  void setUp() {
    var project = new Project();
    project.setId(PROJECT_ID);
    job = new TmsSyncJob();
    job.setId(5L);
    job.setProject(project);
    job.setScopeConfig(new SyncScopeConfig("S", null));

    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(tmsTestFolderMapper.convertFromRemote(any(), any(), any(), any(), any()))
        .thenAnswer(invocation -> invocation.getArgument(1));
  }

  @Test
  void syncFolders_ExistingFolderMovedUnderItsOwnDescendant_KeepsCurrentParent() {
    // Local folder 10 (synced earlier from remote "R") has a descendant 20, chosen as local root now
    var existing = folder(10L, "R", null);
    var remote = RemoteFolder.builder().id("R").name("R").parentId("S").build();
    job.setScopeConfig(new SyncScopeConfig("S", 20L));
    when(tmsTestFolderRepository.findByProjectIdAndExternalIdIn(eq(PROJECT_ID), any()))
        .thenReturn(List.of(existing));
    when(tmsTestFolderRepository.findFolderIdAndParentIdByProjectId(PROJECT_ID))
        .thenReturn(List.of(new Object[]{10L, null}, new Object[]{20L, 10L}));
    when(tmsSyncJobRepository.findById(5L)).thenReturn(Optional.of(job));

    sut.syncFolders(job, List.of(remote), 20L);

    verify(tmsTestFolderMapper).convertFromRemote(eq(remote), eq(existing), any(), isNull(),
        isNull());
    assertEquals(1, job.getErrorLog().getErrors().size());
    assertEquals("R", job.getErrorLog().getErrors().getFirst().getExternalId());
  }

  @Test
  void syncFolders_ExistingFolderWithUnmappedParent_KeepsCurrentParent() {
    // Remote parent "X" is not part of this job, so the folder is processed with forceRootParent
    var existing = folder(10L, "R", 7L);
    var remote = RemoteFolder.builder().id("R").name("R").parentId("X").build();
    when(tmsTestFolderRepository.findByProjectIdAndExternalIdIn(eq(PROJECT_ID), any()))
        .thenReturn(List.of(existing));

    sut.syncFolders(job, List.of(remote), 30L);

    verify(tmsTestFolderMapper).convertFromRemote(eq(remote), eq(existing), any(), eq(7L),
        isNull());
    verify(tmsTestFolderRepository, never()).findFolderIdAndParentIdByProjectId(any());
  }

  @Test
  void syncFolders_ExistingFolderMovedToUnrelatedParent_GetsNextIndexUnderNewParent() {
    var existing = folder(10L, "R", null);
    var remote = RemoteFolder.builder().id("R").name("R").parentId("S").build();
    when(tmsTestFolderRepository.findByProjectIdAndExternalIdIn(eq(PROJECT_ID), any()))
        .thenReturn(List.of(existing));
    when(tmsTestFolderRepository.findFolderIdAndParentIdByProjectId(PROJECT_ID))
        .thenReturn(List.of(new Object[]{10L, null}, new Object[]{30L, null}));
    when(tmsTestFolderRepository.findMaxIndex(PROJECT_ID, 30L)).thenReturn(4);

    sut.syncFolders(job, List.of(remote), 30L);

    verify(tmsTestFolderMapper).convertFromRemote(eq(remote), eq(existing), any(), eq(30L),
        eq(5));
    verify(tmsSyncJobRepository, never()).findById(any());
  }

  private TmsTestFolder folder(Long id, String externalId, Long parentId) {
    var folder = new TmsTestFolder();
    folder.setId(id);
    folder.setExternalId(externalId);
    if (parentId != null) {
      var parent = new TmsTestFolder();
      parent.setId(parentId);
      folder.setParentTestFolder(parent);
    }
    return folder;
  }
}
