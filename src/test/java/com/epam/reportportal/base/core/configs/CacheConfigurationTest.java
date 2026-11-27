/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.persistence.dao.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

class CacheConfigurationTest {

  @Test
  void cachesAreBounded() {
    CacheManager manager = new CacheConfiguration().cacheManager();
    @SuppressWarnings("unchecked")
    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
        (com.github.benmanes.caffeine.cache.Cache<Object, Object>) manager.getCache("bounded").getNativeCache();

    assertEquals(10_000L, nativeCache.policy().eviction().orElseThrow().getMaximum());
  }

  @Test
  void apiKeyLookupMissesAreNotCached() throws Exception {
    Cacheable cacheable = ApiKeyRepository.class.getMethod("findByHash", String.class)
        .getAnnotation(Cacheable.class);

    assertEquals("#result == null", cacheable.unless());
  }
}
