package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsFolderSyncServiceImpl implements TmsFolderSyncService {

  private final TmsTestFolderRepository tmsTestFolderRepository;
  private final TmsTestFolderMapper tmsTestFolderMapper;

  @Override
  @Transactional
  public Map<String, Long> syncFolders(TmsSyncJob job, List<RemoteFolder> remoteFolders,
      Long localRootFolderId) {
    var folderIdMap = new HashMap<String, Long>();
    folderIdMap.put(job.getScopeConfig().getRemoteFolderId(), localRootFolderId);

    var externalIds = remoteFolders.stream().map(RemoteFolder::getId).toList();
    var existingFolders = tmsTestFolderRepository
        .findByProjectIdAndExternalIdIn(job.getProject().getId(), externalIds)
        .stream()
        .collect(Collectors.toMap(TmsTestFolder::getExternalId, f -> f));

    var nextIndexMap = new HashMap<Long, Integer>();

    for (var remoteFolder : remoteFolders) {
      var existing = existingFolders.get(remoteFolder.getId());
      var savedFolder = processFolder(
          job, remoteFolder, existing, localRootFolderId, folderIdMap, nextIndexMap
      );
      folderIdMap.put(remoteFolder.getId(), savedFolder.getId());
    }
    return folderIdMap;
  }

  private TmsTestFolder processFolder(
      TmsSyncJob job,
      RemoteFolder remoteFolder,
      TmsTestFolder existing,
      Long localRootFolderId,
      Map<String, Long> folderIdMap,
      Map<Long, Integer> nextIndexMap) {

    Long parentFolderId;
    Integer index = null;

    if (existing == null) {
      parentFolderId = folderIdMap.getOrDefault(remoteFolder.getParentId(), localRootFolderId);

      index = nextIndexMap.computeIfAbsent(parentFolderId, pid -> {
        var max = tmsTestFolderRepository.findMaxIndex(job.getProject().getId(), pid);
        return max == null ? 0 : max + 1;
      });
      nextIndexMap.put(parentFolderId, index + 1);
    } else {
      parentFolderId = folderIdMap.get(remoteFolder.getParentId());
    }

    var folder = tmsTestFolderMapper.convertFromRemote(
        remoteFolder, existing, job.getProject(), parentFolderId, index
    );

    return tmsTestFolderRepository.save(folder);
  }
}
