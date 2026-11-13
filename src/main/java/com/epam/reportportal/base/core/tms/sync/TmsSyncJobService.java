package com.epam.reportportal.base.core.tms.sync;

import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.TmsSyncJobRS;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for managing test management system synchronization jobs.
 */
public interface TmsSyncJobService {

  /**
   * Starts a synchronization job.
   *
   * @param projectId the project identifier
   * @param integrationId the integration identifier
   * @param remoteFolderId the remote folder identifier
   * @param localFolderId the local folder identifier
   * @return the created synchronization job
   */
  TmsSyncJobRS startSyncJob(Long projectId, Long integrationId, String remoteFolderId, Long localFolderId);

  /**
   * Retrieves synchronization jobs for a project.
   *
   * @param projectId the project identifier
   * @param pageable pagination information
   * @return a page of synchronization jobs
   */
  Page<TmsSyncJobRS> getSyncJobs(Long projectId, Pageable pageable);

  /**
   * Retrieves a synchronization job.
   *
   * @param projectId the project identifier
   * @param jobId the synchronization job identifier
   * @return the synchronization job
   */
  TmsSyncJobRS getSyncJob(Long projectId, Long jobId);

  /**
   * Cancels a synchronization job.
   *
   * @param projectId the project identifier
   * @param jobId the synchronization job identifier
   */
  void cancelSyncJob(Long projectId, Long jobId);

  /**
   * Retrieves remote folders available through an integration.
   *
   * @param projectId the project identifier
   * @param integrationId the integration identifier
   * @param provider the test management system provider
   * @param rootFolderId the root remote folder identifier
   * @return the available remote folders
   */
  List<RemoteFolder> getRemoteFolders(Long projectId, Long integrationId, String provider, String rootFolderId);

  /**
   * Executes a synchronization job.
   *
   * @param jobId the synchronization job identifier
   */
  void executeSync(Long jobId);
}
