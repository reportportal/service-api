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

  @Test
  void syncFolders_ExistingFolderMovedToOtherParent_ClosesIndexGapInOldParent() {
    var existing = folder(10L, "R", 7L);
    existing.setIndex(2);
    var remote = RemoteFolder.builder().id("R").name("R").parentId("S").build();
    when(tmsTestFolderRepository.findByProjectIdAndExternalIdIn(eq(PROJECT_ID), any()))
        .thenReturn(List.of(existing));
    when(tmsTestFolderRepository.findFolderIdAndParentIdByProjectId(PROJECT_ID))
        .thenReturn(List.of(new Object[]{10L, 7L}, new Object[]{30L, null}));
    when(tmsTestFolderRepository.findMaxIndex(PROJECT_ID, 30L)).thenReturn(4);

    sut.syncFolders(job, List.of(remote), 30L);

    verify(tmsTestFolderRepository).shiftIndexes(PROJECT_ID, 7L, 3, -1);
  }

  @Test
  void syncFolders_TwoMovesOutOfSameParentAndSiblingRenamed_KeepsIndexesDenseAndUnique() {
    // Parent 7 holds A(0), B(1), C(2), D(3); A and C move out, B is renamed remotely
    var a = indexed(10L, "A", 7L, 0);
    var b = indexed(11L, "B", 7L, 1);
    var c = indexed(12L, "C", 7L, 2);
    var d = indexed(13L, "D", 7L, 3);
    when(tmsTestFolderRepository.findByProjectIdAndExternalIdIn(eq(PROJECT_ID), any()))
        .thenReturn(List.of(a, b, c, d));
    when(tmsTestFolderRepository.findFolderIdAndParentIdByProjectId(PROJECT_ID))
        .thenReturn(List.of(new Object[]{10L, 7L}, new Object[]{11L, 7L},
            new Object[]{12L, 7L}, new Object[]{13L, 7L}, new Object[]{30L, null}));
    when(tmsTestFolderRepository.findMaxIndex(PROJECT_ID, 30L)).thenReturn(null);
    when(tmsTestFolderMapper.convertFromRemote(any(), any(), any(), any(), any()))
        .thenAnswer(invocation -> {
          TmsTestFolder folder = invocation.getArgument(1);
          Long parentId = invocation.getArgument(3);
          Integer index = invocation.getArgument(4);
          folder.setName(((RemoteFolder) invocation.getArgument(0)).getName());
          folder.setParentTestFolder(parentId == null ? null : folder(parentId, null, null));
          if (index != null) {
            folder.setIndex(index);
          }
          return folder;
        });
    var remotes = List.of(
        RemoteFolder.builder().id("A").name("A").parentId("S").build(),
        RemoteFolder.builder().id("B").name("B renamed").parentId("X").build(),
        RemoteFolder.builder().id("C").name("C").parentId("S").build(),
        RemoteFolder.builder().id("D").name("D").parentId("X").build());

    sut.syncFolders(job, remotes, 30L);

    // In-memory indexes (flushed by dirty checking) match what the bulk shifts did in the DB
    assertEquals(0, b.getIndex());
    assertEquals(1, d.getIndex());
    assertEquals(0, a.getIndex());
    assertEquals(1, c.getIndex());
    verify(tmsTestFolderRepository).shiftIndexes(PROJECT_ID, 7L, 1, -1);
    verify(tmsTestFolderRepository).shiftIndexes(PROJECT_ID, 7L, 2, -1);
  }

  private TmsTestFolder indexed(Long id, String externalId, Long parentId, int index) {
    var folder = folder(id, externalId, parentId);
    folder.setIndex(index);
    return folder;
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
