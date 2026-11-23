package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncError;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmsFolderSyncServiceImpl implements TmsFolderSyncService {

  private final TmsTestFolderRepository tmsTestFolderRepository;
  private final TmsTestFolderMapper tmsTestFolderMapper;
  private final TmsSyncJobRepository tmsSyncJobRepository;

  @Override
  @Transactional
  public Map<String, Long> syncFolders(TmsSyncJob job, List<RemoteFolder> remoteFolders,
      Long localRootFolderId) {
    // Taken before anything is read: a concurrent sync of the project (e.g. one still running after
    // its job was canceled) commits first, and its moves are visible to the cycle checks below
    tmsTestFolderRepository.lockFolderTree(job.getProject().getId());

    var folderIdMap = new HashMap<String, Long>();
    folderIdMap.put(job.getScopeConfig().getRemoteFolderId(), localRootFolderId);

    var externalIds = remoteFolders.stream().map(RemoteFolder::getId).toList();
    var existingFolders = tmsTestFolderRepository
        .findByProjectIdAndExternalIdIn(job.getProject().getId(), externalIds)
        .stream()
        .collect(Collectors.toMap(TmsTestFolder::getExternalId, f -> f));

    var nextIndexMap = new HashMap<Long, Integer>();
    var folderParents = new FolderParents(job.getProject().getId());
    var pendingFolders = new ArrayList<>(remoteFolders);

    while (!pendingFolders.isEmpty()) {
      var processedAny = false;
      var iterator = pendingFolders.iterator();

      while (iterator.hasNext()) {
        var remoteFolder = iterator.next();
        if (remoteFolder.getParentId() != null
            && !folderIdMap.containsKey(remoteFolder.getParentId())) {
          continue;
        }

        var existing = existingFolders.get(remoteFolder.getId());
        var savedFolder = processFolder(
            job, remoteFolder, existing, localRootFolderId, folderIdMap, nextIndexMap,
            folderParents, existingFolders, false
        );
        folderIdMap.put(remoteFolder.getId(), savedFolder.getId());
        iterator.remove();
        processedAny = true;
      }

      if (!processedAny) {
        for (var remoteFolder : pendingFolders) {
          var existing = existingFolders.get(remoteFolder.getId());
          var savedFolder = processFolder(
              job, remoteFolder, existing, localRootFolderId, folderIdMap, nextIndexMap,
              folderParents, existingFolders, true
          );
          folderIdMap.put(remoteFolder.getId(), savedFolder.getId());
        }
        break;
      }
    }

    return folderIdMap;
  }

  private TmsTestFolder processFolder(
      TmsSyncJob job,
      RemoteFolder remoteFolder,
      TmsTestFolder existing,
      Long localRootFolderId,
      Map<String, Long> folderIdMap,
      Map<Long, Integer> nextIndexMap,
      FolderParents folderParents,
      Map<String, TmsTestFolder> existingFolders,
      boolean forceRootParent) {

    Long parentFolderId;
    Integer index = null;

    if (existing == null) {
      parentFolderId = forceRootParent
          ? localRootFolderId
          : folderIdMap.getOrDefault(remoteFolder.getParentId(), localRootFolderId);
      index = nextIndex(job, parentFolderId, nextIndexMap);
    } else {
      // An existing folder moves only to the remote parent mapped in this job and never into its
      // own subtree; otherwise it keeps its current parent.
      var currentParentId = existing.getParentTestFolder() != null
          ? existing.getParentTestFolder().getId()
          : null;
      parentFolderId = currentParentId;
      if (!forceRootParent && folderIdMap.containsKey(remoteFolder.getParentId())) {
        var newParentId = folderIdMap.get(remoteFolder.getParentId());
        if (!Objects.equals(newParentId, currentParentId)) {
          if (folderParents.createsCycle(existing.getId(), newParentId)) {
            recordCycleError(job, remoteFolder, existing.getId(), newParentId);
          } else {
            // Close the gap the folder leaves in its old parent
            if (existing.getIndex() != null) {
              tmsTestFolderRepository.shiftIndexes(job.getProject().getId(), currentParentId,
                  existing.getIndex() + 1, -1);
              // The bulk update does not touch the loaded entities, and a sibling dirtied later in
              // this sync (e.g. renamed) would flush its stale index back: mirror the shift
              existingFolders.values().forEach(sibling -> {
                var siblingParentId = sibling.getParentTestFolder() != null
                    ? sibling.getParentTestFolder().getId()
                    : null;
                if (Objects.equals(siblingParentId, currentParentId) && sibling.getIndex() != null
                    && sibling.getIndex() > existing.getIndex()) {
                  sibling.setIndex(sibling.getIndex() - 1);
                }
              });
              nextIndexMap.computeIfPresent(currentParentId, (pid, next) -> next - 1);
            }
            parentFolderId = newParentId;
            index = nextIndex(job, parentFolderId, nextIndexMap);
          }
        }
      }
    }

    var folder = tmsTestFolderMapper.convertFromRemote(
        remoteFolder, existing, job.getProject(), parentFolderId, index
    );

    var saved = tmsTestFolderRepository.save(folder);
    folderParents.put(saved.getId(), parentFolderId);
    return saved;
  }

  private Integer nextIndex(TmsSyncJob job, Long parentFolderId,
      Map<Long, Integer> nextIndexMap) {
    var index = nextIndexMap.computeIfAbsent(parentFolderId, pid -> {
      var max = tmsTestFolderRepository.findMaxIndex(job.getProject().getId(), pid);
      return max == null ? 0 : max + 1;
    });
    nextIndexMap.put(parentFolderId, index + 1);
    return index;
  }

  private void recordCycleError(TmsSyncJob job, RemoteFolder remoteFolder, Long folderId,
      Long newParentId) {
    var message = "Folder %d was not moved under folder %d: that folder is inside its subtree"
        .formatted(folderId, newParentId);
    log.warn("TMS Sync Job {}: {}", job.getId(), message);
    tmsSyncJobRepository.findById(job.getId()).ifPresent(syncJob -> {
      if (syncJob.getErrorLog() == null) {
        syncJob.setErrorLog(new SyncErrorLog(new ArrayList<>()));
      }
      syncJob.getErrorLog().getErrors().add(new SyncError(remoteFolder.getId(), message, null));
      tmsSyncJobRepository.save(syncJob);
    });
  }

  /**
   * Folder id to parent id map of the project, loaded once per sync on first need and kept up to
   * date with the folders saved by the sync.
   */
  private final class FolderParents {

    private final Long projectId;
    private Map<Long, Long> parents;

    private FolderParents(Long projectId) {
      this.projectId = projectId;
    }

    private boolean createsCycle(Long folderId, Long newParentId) {
      if (parents == null) {
        parents = new HashMap<>();
        tmsTestFolderRepository.findFolderIdAndParentIdByProjectId(projectId)
            .forEach(row -> parents.put((Long) row[0], (Long) row[1]));
      }
      var visited = new HashSet<Long>();
      for (var current = newParentId; current != null; current = parents.get(current)) {
        if (current.equals(folderId) || !visited.add(current)) {
          return true;
        }
      }
      return false;
    }

    private void put(Long folderId, Long parentId) {
      if (parents != null) {
        parents.put(folderId, parentId);
      }
    }
  }
}
