package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsDatasetRepository extends ReportPortalRepository<TmsDataset, Long> {

  @Query("SELECT ds FROM TmsDataset ds " +
      "LEFT JOIN FETCH ds.data atr " +
      "WHERE ds.project.id = :projectId AND ds.id = :id"
  )
  Optional<TmsDataset> findByIdAndProjectId(Long id, long projectId);

  List<TmsDataset> findAllByProject_Id(Long projectId);

  @Modifying
  void deleteByIdAndProject_Id(Long id, Long projectId);

  Optional<TmsDataset> findByName(String name);
}
