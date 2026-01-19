package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsAttributeRepository extends ReportPortalRepository<TmsAttribute, Long> {

  boolean existsByKeyAndProject_Id(String key, Long projectId);

  List<TmsAttribute> findAllByProject_Id(Long projectId);

  Optional<TmsAttribute> findByKeyAndProject_Id(String key, Long projectId);

  Optional<TmsAttribute> findByIdAndProject_Id(Long id, Long projectId);

  List<TmsAttribute> findAllByProject_IdAndKeyIn(Long projectId, Set<String> keys);
}