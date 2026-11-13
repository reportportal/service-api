package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import java.util.List;
import java.util.Map;

/**
 * Service for synchronizing folders between a TMS and ReportPortal.
 */
public interface TmsFolderSyncService {

  /**
   * Synchronizes remote folders with the local folder structure.
   *
   * @param job the TMS synchronization job
   * @param remoteFolders the remote folders to synchronize
   * @param localRootFolderId the identifier of the local root folder
   * @return a map of remote folder identifiers to local folder identifiers
   */
  Map<String, Long> syncFolders(TmsSyncJob job, List<RemoteFolder> remoteFolders, Long localRootFolderId);
}
