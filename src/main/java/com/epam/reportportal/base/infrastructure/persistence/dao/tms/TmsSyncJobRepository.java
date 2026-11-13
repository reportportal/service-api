package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.core.tms.enums.TmsSyncStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsSyncJobRepository extends ReportPortalRepository<TmsSyncJob, Long> {

  /**
   * Finds synchronization jobs for a project.
   *
   * @param projectId project identifier
   * @param pageable pagination information
   * @return a page of synchronization jobs
   */
  Page<TmsSyncJob> findByProjectId(Long projectId, Pageable pageable);

  /**
   * Finds synchronization jobs with the specified statuses.
   *
   * @param statuses synchronization statuses
   * @return synchronization jobs with matching statuses
   */
  List<TmsSyncJob> findByStatusIn(Collection<TmsSyncStatus> statuses);

  /**
   * Checks whether a synchronization job exists for the project, integration, and statuses.
   *
   * @param projectId project identifier
   * @param integrationId integration identifier
   * @param statuses synchronization statuses
   * @return {@code true} if a matching job exists
   */
  boolean existsByProjectIdAndIntegrationIdAndStatusIn(
      Long projectId, Long integrationId, Collection<TmsSyncStatus> statuses
  );

  /**
   * Finds the latest synchronization job with the specified status.
   *
   * @param projectId project identifier
   * @param integrationId integration identifier
   * @param status synchronization status
   * @return the latest matching synchronization job
   */
  Optional<TmsSyncJob> findFirstByProjectIdAndIntegrationIdAndStatusOrderByCompletedAtDesc(
      Long projectId, Long integrationId, TmsSyncStatus status
  );

  /**
   * Finds a synchronization job with its integration.
   *
   * @param jobId synchronization job identifier
   * @return the synchronization job with its integration
   */
  @Query("select job "
      + "from TmsSyncJob job "
      + "join fetch job.integration "
      + "where job.id = :jobId")
  Optional<TmsSyncJob> findByIdWithIntegration(@Param("jobId") Long jobId);

  /**
   * Finds the latest successful synchronization job for a remote folder.
   *
   * @param remoteFolderId remote folder identifier
   * @return the latest successful synchronization job
   */
  @Query(value = "SELECT * FROM tms_sync_job "
      + "WHERE status = 'SUCCESS' AND scope_config ->> 'remoteFolderId' = :remoteFolderId "
      + "ORDER BY completed_at DESC "
      + "LIMIT 1",
      nativeQuery = true)
  Optional<TmsSyncJob> findLatestSuccessfulByRemoteFolderId(@Param("remoteFolderId") String remoteFolderId);

  /**
   * Finds the latest synchronization job for a remote folder and status.
   *
   * @param projectId project identifier
   * @param integrationId integration identifier
   * @param status synchronization status
   * @param remoteFolderId remote folder identifier
   * @return the latest matching synchronization job
   */
  @Query(value = "SELECT * FROM tms_sync_job "
      + "WHERE project_id = :projectId "
      + "AND integration_id = :integrationId "
      + "AND status = :#{#status.name()} "
      + "AND scope_config ->> 'remoteFolderId' IS NOT DISTINCT FROM :remoteFolderId "
      + "ORDER BY completed_at DESC "
      + "LIMIT 1",
      nativeQuery = true)
  Optional<TmsSyncJob> findFirstByProjectIdAndIntegrationIdAndStatusAndRemoteFolderIdOrderByCompletedAtDesc(
      @Param("projectId") Long projectId,
      @Param("integrationId") Long integrationId,
      @Param("status") TmsSyncStatus status,
      @Param("remoteFolderId") String remoteFolderId
  );

  /**
   * Finds pending synchronization job identifiers for execution while locking them.
   *
   * @param limit maximum number of job identifiers
   * @return locked pending synchronization job identifiers
   */
  @Query(value = "SELECT id FROM tms_sync_job "
      + "WHERE status = 'PENDING' "
      + "ORDER BY created_at ASC "
      + "LIMIT :limit "
      + "FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<Long> findPendingJobIdsForExecution(@Param("limit") int limit);

  /**
   * Marks stale in-progress synchronization jobs as failed.
   *
   * @param staleThreshold threshold before which jobs are considered stale
   * @param completedAt completion timestamp to assign to failed jobs
   * @return number of synchronization jobs marked as failed
   */
  @Modifying
  @Query(value = "UPDATE tms_sync_job "
      + "SET status = 'FAILED', completed_at = :completedAt "
      + "WHERE status = 'IN_PROGRESS' AND started_at < :staleThreshold",
      nativeQuery = true)
  int markStaleInProgressJobsAsFailed(
      @Param("staleThreshold") Instant staleThreshold,
      @Param("completedAt") Instant completedAt
  );
}
