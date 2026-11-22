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

package com.epam.reportportal.base.core.group;

import com.epam.reportportal.base.auth.userdetails.UserAuthDataCacheEvictor;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Clears the caches of group-derived project access ({@code groupUserProjectRolesCache} and
 * {@code groupProjectDetailsCache}, keyed by user and project) when group membership or group-to-project assignment
 * changes.
 *
 * <p>One group change can touch many users and projects while the keys are per user and project, so the whole of both
 * caches is cleared; such changes are rare. Eviction runs immediately and again after the surrounding transaction
 * commits, like {@link UserAuthDataCacheEvictor}: the second pass removes an entry that a concurrent request
 * re-populated from the not yet committed (old) rows.
 *
 * <p>The caches are per JVM: this affects only the node that runs the eviction. Other replicas keep serving their
 * cached value until it expires (30 seconds).
 */
@Component
public class GroupAccessCacheEvictor {

  public static final String USER_PROJECT_ROLES_CACHE = "groupUserProjectRolesCache";
  public static final String PROJECT_DETAILS_CACHE = "groupProjectDetailsCache";

  private static final List<String> CACHE_NAMES = List.of(USER_PROJECT_ROLES_CACHE, PROJECT_DETAILS_CACHE);

  private final CacheManager cacheManager;

  public GroupAccessCacheEvictor(@Qualifier("caffeineCacheManager") CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Clears both caches now and after the current transaction commits (if there is one).
   */
  public void evictAll() {
    clear();
    UserAuthDataCacheEvictor.afterCommit(this::clear);
  }

  private void clear() {
    for (String name : CACHE_NAMES) {
      Cache cache = cacheManager.getCache(name);
      if (cache != null) {
        cache.clear();
      }
    }
  }
}
