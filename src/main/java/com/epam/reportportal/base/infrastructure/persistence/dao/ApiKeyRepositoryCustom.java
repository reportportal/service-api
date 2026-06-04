package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.user.ApiKey;
import java.time.LocalDate;

/**
 * ApiKey repository custom methods.
 *
 * @author Ivan_Kustau
 */
public interface ApiKeyRepositoryCustom {

  /**
   * Update lastUsedAt for apiKey.
   *
   * @param id         id of the ApiKey to update
   * @param hash       hash of ApiKey to update
   * @param lastUsedAt {@link LocalDate}
   * @return updated version of {@link ApiKey}
   */
  ApiKey updateLastUsedAt(Long id, String hash, LocalDate lastUsedAt);
}
