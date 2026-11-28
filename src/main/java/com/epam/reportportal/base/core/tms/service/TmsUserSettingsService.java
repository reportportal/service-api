package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRS;

/**
 * TMS list settings (items per page, sort mode, tree view mode, ...) stored per user and per
 * project.
 */
public interface TmsUserSettingsService {

  /**
   * Returns the settings of the user in the project.
   *
   * @param projectId project identifier
   * @param userId    user identifier
   * @return stored settings, empty if nothing is stored
   */
  TmsUserSettingsRS get(long projectId, long userId);

  /**
   * Sets or removes the given keys. A {@code null} value removes the key; keys that are not in the
   * request stay unchanged.
   *
   * @param projectId project identifier
   * @param userId    user identifier
   * @param request   keys to set or remove
   * @return all settings of the user in the project after the update
   */
  TmsUserSettingsRS update(long projectId, long userId, TmsUserSettingsRQ request);
}
