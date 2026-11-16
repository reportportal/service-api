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

package com.epam.reportportal.base.core.item.identity;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import java.util.Map;

/**
 * Produces a stable test case id hash from a test item and parents.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface TestCaseHashGenerator {

  Integer generate(TestItem item, List<Long> parentIds, Long projectId);

  /**
   * Same as {@link #generate(TestItem, List, Long)}, but reuses a caller-provided cache of parent
   * item names ({@code testItemNamesByIds}, keyed by item id) to avoid re-fetching names already
   * resolved for other test cases in the same batch. Names resolved during this call are added to
   * the cache so callers can reuse it for subsequent invocations.
   */
  default Integer generate(TestItem item, List<Long> parentIds, Long projectId,
      Map<Long, String> testItemNamesByIds) {
    return generate(item, parentIds, projectId);
  }
}
