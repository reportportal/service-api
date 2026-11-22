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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class UserAuthDataCacheEvictorTest {

  private final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
  private final UserAuthDataCacheEvictor evictor = new UserAuthDataCacheEvictor(cacheManager);

  @AfterEach
  void clearSynchronization() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void evictsOldAndNewLoginAndExternalIdImmediately() {
    Cache cache = cacheManager.getCache(UserAuthDataCacheEvictor.CACHE_NAME);
    cache.put("login_old@x.com", "a");
    cache.put("login_new@x.com", "b");
    cache.put("externalId_ext", "c");
    cache.put("login_other@x.com", "d");

    evictor.evictUser("ext", "old@x.com", "new@x.com");

    assertThat(cache.get("login_old@x.com")).isNull();
    assertThat(cache.get("login_new@x.com")).isNull();
    assertThat(cache.get("externalId_ext")).isNull();
    assertThat(cache.get("login_other@x.com")).isNotNull();
  }

  @Test
  void evictsAgainAfterCommitWhenEntryWasRepopulatedFromTheOldRow() {
    Cache cache = cacheManager.getCache(UserAuthDataCacheEvictor.CACHE_NAME);
    TransactionSynchronizationManager.initSynchronization();

    evictor.evictUser(null, "old@x.com");
    // a concurrent login re-populates the entry from the not yet committed row
    cache.put("login_old@x.com", "stale");

    TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

    assertThat(cache.get("login_old@x.com")).isNull();
  }
}
