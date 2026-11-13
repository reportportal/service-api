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

package com.epam.reportportal.base.core.analyzer.auto.impl.preparer;

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexTestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.Collection;
import java.util.List;

/**
 * Strategy interface for preparing test items for analysis. Provides different preparation strategies based on test
 * item characteristics.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface TestItemPreparationStrategy {

  /**
   * Prepares test items for indexing with specific strategy.
   *
   * @param launchId  the launch ID
   * @param testItems collection of test items to prepare
   * @return prepared list of {@link IndexTestItem} for indexing
   */
  List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems);
}
