package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsMilestoneRepository extends ReportPortalRepository<TmsMilestone, Long> {

  /**
   * Finds a milestone by its ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return optional milestone
   */
  @Query("SELECT m FROM TmsMilestone m WHERE m.id = :id AND m.project.id = :projectId")
  Optional<TmsMilestone> findByIdAndProjectId(@Param("id") Long id,
      @Param("projectId") Long projectId);

  /**
   * Finds all milestones by project ID with pagination.
   *
   * @param projectId the project ID
   * @param pageable  pagination parameters
   * @return page of milestones
   */
  @Query("SELECT m FROM TmsMilestone m WHERE m.project.id = :projectId")
  Page<TmsMilestone> findAllByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  /**
   * Checks if a milestone exists by ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return true if exists, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM TmsMilestone m "
      + "WHERE m.id = :id AND m.project.id = :projectId")
  boolean existsByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

  /**
   * Deletes a milestone by ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return number of deleted records
   */
  @Modifying
  @Query("DELETE FROM TmsMilestone m WHERE m.id = :id AND m.project.id = :projectId")
  int deleteByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);
}
