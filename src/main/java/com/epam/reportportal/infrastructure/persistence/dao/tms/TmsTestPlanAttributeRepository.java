package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttributeId;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for TmsTestPlanAttribute junction entity.
 */
@Repository
public interface TmsTestPlanAttributeRepository
    extends ReportPortalRepository<TmsTestPlanAttribute, TmsTestPlanAttributeId> {

  /**
   * Deletes all attribute associations for a given test plan.
   *
   * @param testPlanId the test plan ID
   */
  @Modifying
  @Query("DELETE FROM TmsTestPlanAttribute tpa WHERE tpa.id.testPlanId = :testPlanId")
  void deleteAllByTestPlanId(@Param("testPlanId") Long testPlanId);

  /**
   * Finds all attribute associations for a given test plan.
   *
   * @param testPlanId the test plan ID
   * @return set of TmsTestPlanAttribute entities
   */
  Set<TmsTestPlanAttribute> findAllByTestPlanId(Long testPlanId);

  /**
   * Checks if an association exists between test plan and item attribute.
   *
   * @param testPlanId the test plan ID
   * @param itemAttributeId the item attribute ID
   * @return true if association exists
   */
  boolean existsByIdTestPlanIdAndIdItemAttributeId(Long testPlanId, Long itemAttributeId);
}