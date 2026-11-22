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

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Clears the group-derived access caches after any write through the core group repositories
 * ({@code GroupRepository}, {@code GroupUserRepository}, {@code GroupProjectRepository},
 * {@code GroupMembershipRepository}), so no caller (core or a plugin sharing these beans) can forget it.
 *
 * <p>Matches methods named {@code save*} and {@code delete*} (inherited from Spring Data or declared); a custom write
 * query added to these repositories later must follow that naming or evict explicitly. Writes that bypass these
 * repository beans (plugin's own data source, raw SQL, DB cascades) are not seen.
 */
@Aspect
@Component
public class GroupAccessCacheEvictionAspect {

  private final GroupAccessCacheEvictor evictor;

  public GroupAccessCacheEvictionAspect(GroupAccessCacheEvictor evictor) {
    this.evictor = evictor;
  }

  /**
   * Runs after a repository write, whether it returned or threw (a partial failure must not leave stale entries).
   */
  @After("""
      execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupRepository+.save*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupRepository+.delete*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupUserRepository+.save*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupUserRepository+.delete*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupProjectRepository+.save*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupProjectRepository+.delete*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupMembershipRepository+.save*(..))
        || execution(* com.epam.reportportal.base.infrastructure.persistence.dao.GroupMembershipRepository+.delete*(..))
      """)
  public void evictAfterGroupWrite() {
    evictor.evictAll();
  }
}
