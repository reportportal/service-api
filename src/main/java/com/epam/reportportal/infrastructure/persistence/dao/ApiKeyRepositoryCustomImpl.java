package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.user.ApiKey;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.transaction.annotation.Transactional;

/**
 * ApiKey repository custom method's implementation.
 *
 * @author Ivan_Kustau
 */
public class ApiKeyRepositoryCustomImpl implements ApiKeyRepositoryCustom {

  @Autowired
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
