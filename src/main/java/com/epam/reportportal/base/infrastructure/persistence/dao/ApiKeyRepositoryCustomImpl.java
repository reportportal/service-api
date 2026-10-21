package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.entity.user.ApiKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.transaction.annotation.Transactional;

/**
 * ApiKey repository custom method's implementation.
 *
 * @author Ivan_Kustau
 */
public class ApiKeyRepositoryCustomImpl implements ApiKeyRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  @Override
  @CachePut(value = "apiKeyCache", key = "#hash")
  public ApiKey updateLastUsedAt(Long id, String hash, LocalDate lastUsedAt) {
    ApiKey apiKey = entityManager.find(ApiKey.class, id);

    if (apiKey != null) {
      apiKey.setLastUsedAt(lastUsedAt);
      entityManager.merge(apiKey);
    }

    return apiKey;
  }
}
