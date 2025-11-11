/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.user.ApiKey;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * ApiKey Repository
 *
 * @author Andrei Piankouski
 */
@Repository
public interface ApiKeyRepository
    extends ReportPortalRepository<ApiKey, Long>, ApiKeyRepositoryCustom {

  /**
   * @param hash hash of api key
   * @return {@link ApiKey}
   */
  @Cacheable(value = "apiKeyCache", key = "#hash", cacheResolver = "apiKeyCacheResolver")
  ApiKey findByHash(String hash);

  /**
   * @param name   name of user Api key
   * @param userId {@link com.epam.reportportal.infrastructure.persistence.entity.user.User#id}
   * @return if exists 'true' else 'false'
   */
  boolean existsByNameAndUserId(String name, Long userId);

  /**
   * @param id     id of the Api key
   * @param userId {@link com.epam.reportportal.infrastructure.persistence.entity.user.User#id}
   * @return if exists 'true' else 'false'
   */
  boolean existsByIdAndUserId(Long id, Long userId);

  /**
   * @param userId {@link com.epam.reportportal.infrastructure.persistence.entity.user.User#id}
   * @return list of user api keys
   */
  List<ApiKey> findByUserId(Long userId);

  @CacheEvict(value = "apiKeyCache", cacheResolver = "apiKeyCacheResolver")
  @Override
  void deleteById(@NonNull Long id);
}
