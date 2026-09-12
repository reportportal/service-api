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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsSyncJobRepository extends ReportPortalRepository<TmsSyncJob, Long> {

  Page<TmsSyncJob> findByProjectId(Long projectId, Pageable pageable);

  List<TmsSyncJob> findByStatusIn(Collection<TmsSyncStatus> statuses);

  boolean existsByProjectIdAndIntegrationIdAndStatusIn(
      Long projectId, Long integrationId, Collection<TmsSyncStatus> statuses
  );

  Optional<TmsSyncJob> findFirstByProjectIdAndIntegrationIdAndStatusOrderByCompletedAtDesc(
      Long projectId, Long integrationId, TmsSyncStatus status
  );

  @Query("select job "
      + "from TmsSyncJob job "
      + "join fetch job.integration "
      + "where job.id = :jobId")
  Optional<TmsSyncJob> findByIdWithIntegration(@Param("jobId") Long jobId);

  @Query(value = "SELECT * FROM tms_sync_job "
      + "WHERE status = 'SUCCESS' AND scope_config ->> 'remoteFolderId' = :remoteFolderId "
      + "ORDER BY completed_at DESC "
      + "LIMIT 1",
      nativeQuery = true)
  Optional<TmsSyncJob> findLatestSuccessfulByRemoteFolderId(@Param("remoteFolderId") String remoteFolderId);

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

  @Query(value = "SELECT id FROM tms_sync_job "
      + "WHERE status = 'PENDING' "
      + "ORDER BY created_at ASC "
      + "LIMIT :limit "
      + "FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<Long> findPendingJobIdsForExecution(@Param("limit") int limit);

  @Query(value = "SELECT id FROM tms_sync_job "
      + "WHERE status = 'IN_PROGRESS' AND started_at < :staleThreshold "
      + "FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<Long> findStaleInProgressJobIds(@Param("staleThreshold") Instant staleThreshold);
}
