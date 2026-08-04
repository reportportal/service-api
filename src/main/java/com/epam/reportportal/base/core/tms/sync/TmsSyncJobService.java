package com.epam.reportportal.base.core.tms.sync;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.TmsSyncJobRS;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TmsSyncJobService {

    TmsSyncJobRS startSyncJob(Long projectId, Long integrationId, String remoteFolderId, Long localFolderId);

    Page<TmsSyncJobRS> getSyncJobs(Long projectId, Pageable pageable);

    TmsSyncJobRS getSyncJob(Long projectId, Long jobId);

    void cancelSyncJob(Long projectId, Long jobId);

    List<RemoteFolder> getRemoteFolders(Long projectId, Long integrationId, String provider, String rootFolderId);

    void executeSync(Long jobId);
}