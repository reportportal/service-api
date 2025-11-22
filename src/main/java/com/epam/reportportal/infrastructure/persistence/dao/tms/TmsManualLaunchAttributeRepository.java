package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsManualLaunchAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsManualLaunchAttributeId;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link TmsManualLaunchAttribute} entity.
 */
public interface TmsManualLaunchAttributeRepository extends
    ReportPortalRepository<TmsManualLaunchAttribute, TmsManualLaunchAttributeId> {

  /**
   * Finds all attributes by launch ID.
   *
   * @param launchId launch ID
   * @return list of attributes
   */
  @Query("SELECT mla FROM TmsManualLaunchAttribute mla WHERE mla.launch.id = :launchId")
  List<TmsManualLaunchAttribute> findByLaunchId(@Param("launchId") Long launchId);

  /**
   * Deletes all attributes by launch ID.
   *
   * @param launchId launch ID
   */
  @Modifying
  @Query("DELETE FROM TmsManualLaunchAttribute mla WHERE mla.launch.id = :launchId")
  void deleteByLaunchId(@Param("launchId") Long launchId);

  /**
   * Checks if attribute exists for launch.
   *
   * @param attributeId attribute ID
   * @param launchId    launch ID
   * @return true if exists
   */
  @Query("SELECT CASE WHEN COUNT(mla) > 0 THEN true ELSE false END FROM TmsManualLaunchAttribute mla " +
      "WHERE mla.attribute.id = :attributeId AND mla.launch.id = :launchId")
  boolean existsByAttributeIdAndLaunchId(@Param("attributeId") Long attributeId,
      @Param("launchId") Long launchId);
}
