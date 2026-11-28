package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettingsId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link TmsUserSettings}.
 */
public interface TmsUserSettingsRepository extends
    ReportPortalRepository<TmsUserSettings, TmsUserSettingsId> {

  /**
   * Merges settings into the row of the user and project in one statement, so concurrent updates
   * of different keys do not overwrite each other. Creates the row if it does not exist.
   *
   * @param userId      user identifier
   * @param projectId   project identifier
   * @param values      JSON object with the keys to set
   * @param removedKeys JSON array with the keys to remove
   * @return number of affected rows
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "INSERT INTO tms_user_settings (user_id, project_id, settings, updated_at) "
      + "VALUES (:userId, :projectId, CAST(:values AS jsonb), now()) "
      + "ON CONFLICT (user_id, project_id) DO UPDATE "
      + "SET settings = (tms_user_settings.settings || CAST(:values AS jsonb)) "
      + "- ARRAY(SELECT jsonb_array_elements_text(CAST(:removedKeys AS jsonb))), "
      + "updated_at = now()",
      nativeQuery = true)
  int upsertSettings(@Param("userId") Long userId, @Param("projectId") Long projectId,
      @Param("values") String values, @Param("removedKeys") String removedKeys);
}
