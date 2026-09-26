package com.epam.reportportal.base.infrastructure.persistence.dao.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long>,
    JpaSpecificationExecutor<Pipeline> {

  Optional<Pipeline> findByProjectIdAndName(Long projectId, String name);

  /**
   * Locks the pipeline row for the rest of the caller's transaction, serializing
   * concurrent iteration-number assignment for the same pipeline.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Pipeline p where p.id = :id")
  Optional<Pipeline> findByIdForUpdate(@Param("id") Long id);
}
