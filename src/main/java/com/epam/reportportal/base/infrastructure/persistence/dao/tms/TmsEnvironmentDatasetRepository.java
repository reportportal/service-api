package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsEnvironmentDataset;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsEnvironmentDatasetId;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TmsEnvironmentDatasetRepository extends
    ReportPortalRepository<TmsEnvironmentDataset, TmsEnvironmentDatasetId> {

  @Modifying
  void deleteAllByDataset_Id(Long datasetId);

  /**
   * Counts the environments among the given ids that belong to the project.
   *
   * @param environmentIds environment ids
   * @param projectId      project id
   * @return number of matching environments of the project
   */
  @Query("SELECT COUNT(e) FROM TmsEnvironment e "
      + "WHERE e.id IN :environmentIds AND e.project.id = :projectId")
  long countEnvironmentsByIdsAndProjectId(
      @Param("environmentIds") Collection<Long> environmentIds,
      @Param("projectId") Long projectId);
}
