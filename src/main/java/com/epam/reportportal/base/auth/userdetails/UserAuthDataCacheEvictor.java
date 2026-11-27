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

package com.epam.reportportal.base.auth.userdetails;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Evicts entries of {@code userAuthDataCache} (user-details data used at login, keyed by {@code login_<login>} and
 * {@code externalId_<externalId>}) when a user mutation makes them stale.
 *
 * <p>Eviction runs immediately and again after the surrounding transaction commits: the first one makes the change
 * visible to this request, the second removes an entry that a concurrent login re-populated from the not yet committed
 * (old) row.
 */
@Component
public class UserAuthDataCacheEvictor {

  public static final String CACHE_NAME = "userAuthDataCache";
  public static final String LOGIN_KEY_PREFIX = "login_";
  public static final String EXTERNAL_ID_KEY_PREFIX = "externalId_";

  private final CacheManager cacheManager;

  public UserAuthDataCacheEvictor(@Qualifier("caffeineCacheManager") CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Evicts the entries of the given logins and external ids now and after the current transaction commits.
   *
   * @param logins      logins to evict (nulls ignored)
   * @param externalIds external ids to evict (nulls ignored)
   */
  public void evict(String[] logins, String... externalIds) {
    String[] loginKeys = Arrays.stream(logins).filter(Objects::nonNull).map(l -> LOGIN_KEY_PREFIX + l)
        .toArray(String[]::new);
    String[] extKeys = Stream.of(externalIds).filter(Objects::nonNull).map(e -> EXTERNAL_ID_KEY_PREFIX + e)
        .toArray(String[]::new);
    evictKeys(loginKeys);
    evictKeys(extKeys);
    afterCommit(() -> {
      evictKeys(loginKeys);
      evictKeys(extKeys);
    });
  }

  /**
   * Convenience for a single user state change.
   *
   * @param externalId the user's external id, may be {@code null}
   * @param logins     logins to evict
   */
  public void evictUser(String externalId, String... logins) {
    evict(logins, externalId);
  }

  private void evictKeys(String[] keys) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      for (String key : keys) {
        cache.evict(key);
      }
    }
  }

  /**
   * Runs the action once the current transaction commits; if there is no transaction it has already been executed by
   * the immediate eviction, so nothing more is scheduled.
   */
  public static void afterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          action.run();
        }
      });
    }
  }
}
