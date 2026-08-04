package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import java.util.List;
import java.util.Map;

public interface TmsFolderSyncService {

  Map<String, Long> syncFolders(TmsSyncJob job, List<RemoteFolder> remoteFolders, Long localRootFolderId);
}
