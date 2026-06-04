/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.infrastructure.persistence.dao.ApiKeyRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ApiKey;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Component for resolving {@link ApiKey} cache by {@link ApiKey#id} field.
 *
 * @author Ivan_Kustau
 */
@Component("apiKeyCacheResolver")
public class ApiKeyCacheResolver implements CacheResolver {

  private final ApiKeyRepository apiKeyRepository;

  private final CacheManager cacheManager;

  public ApiKeyCacheResolver(ApiKeyRepository apiKeyRepository,
      @Qualifier("caffeineCacheManager") CacheManager cacheManager) {
    this.apiKeyRepository = apiKeyRepository;
    this.cacheManager = cacheManager;
  }

  @Override
  @NonNull
  public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
    Cache cache = cacheManager.getCache("apiKeyCache");
    Collection<Cache> caches = new ArrayList<>();

    if (cache != null) {
      caches.add(cache);

      Object arg = context.getArgs()[0];

      if (context.getMethod().getName().equals("deleteById") && arg instanceof Long) {
        Long apiKeyId = (Long) arg;
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId).orElse(null);
        if (apiKey != null) {
          cache.evict(apiKey.getHash());
        }
      }
    }

    return caches;
  }
}
