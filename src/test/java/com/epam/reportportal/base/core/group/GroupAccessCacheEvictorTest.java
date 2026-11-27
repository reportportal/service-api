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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class GroupAccessCacheEvictorTest {

  private final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
  private final GroupAccessCacheEvictor evictor = new GroupAccessCacheEvictor(cacheManager);

  @AfterEach
  void clearSynchronization() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void clearsBothCachesImmediatelyAndLeavesOthersAlone() {
    Cache roles = cacheManager.getCache(GroupAccessCacheEvictor.USER_PROJECT_ROLES_CACHE);
    Cache details = cacheManager.getCache(GroupAccessCacheEvictor.PROJECT_DETAILS_CACHE);
    Cache other = cacheManager.getCache("userAuthDataCache");
    roles.put("1_2", "x");
    roles.put("3_4", "y");
    details.put("1_key", "z");
    other.put("login_a", "keep");

    evictor.evictAll();

    assertThat(roles.get("1_2")).isNull();
    assertThat(roles.get("3_4")).isNull();
    assertThat(details.get("1_key")).isNull();
    assertThat(other.get("login_a")).isNotNull();
  }

  @Test
  void clearsAgainAfterCommitWhenEntryWasRepopulatedFromTheOldRows() {
    Cache roles = cacheManager.getCache(GroupAccessCacheEvictor.USER_PROJECT_ROLES_CACHE);
    TransactionSynchronizationManager.initSynchronization();

    evictor.evictAll();
    // a concurrent request re-populates the entry from the not yet committed rows
    roles.put("1_2", "stale");
    assertThat(roles.get("1_2")).isNotNull();

    TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

    assertThat(roles.get("1_2")).isNull();
  }

  @Test
  void doesNotFailWithoutTransaction() {
    evictor.evictAll();
    assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();
  }
}
