package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TmsSequenceGeneratorRepository {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Generates next display ID for TMS entities using PostgreSQL function.
   */
  public String generateDisplayId(Long projectId, String entityType, String prefix) {
    return jdbcTemplate.queryForObject(
        "SELECT generate_tms_display_id(?, ?, ?)",
        String.class,
        projectId,
        entityType,
        prefix
    );
  }
}
