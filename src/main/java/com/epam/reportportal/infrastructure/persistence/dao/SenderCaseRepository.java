package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.project.email.SenderCase;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SenderCaseRepository extends ReportPortalRepository<SenderCase, Long> {

  @Query(value = "SELECT sc FROM SenderCase sc WHERE sc.project.id = :projectId ORDER BY sc.id")
  List<SenderCase> findAllByProjectId(@Param(value = "projectId") Long projectId);

  Optional<SenderCase> findByProjectIdAndTypeAndRuleNameIgnoreCase(Long projectId, String ruleType, String ruleName);

  @Modifying
  @Query(value = "DELETE FROM recipients WHERE sender_case_id = :id AND recipient IN (:recipients)", nativeQuery = true)
  int deleteRecipients(@Param(value = "id") Long id,
      @Param(value = "recipients") Collection<String> recipients);

  @Modifying
  @Query(value = "DELETE FROM sender_case WHERE id = :id", nativeQuery = true)
  int deleteSenderCaseById(@Param(value = "id") Long id);
}
