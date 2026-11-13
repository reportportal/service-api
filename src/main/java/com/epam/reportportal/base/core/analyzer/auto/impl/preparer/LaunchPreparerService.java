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

import com.epam.reportportal.base.infrastructure.model.analyzer.IndexLaunch;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import java.util.Optional;

/**
 * Builds an indexable launch snapshot with items for the analyzer service.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LaunchPreparerService {

  Optional<IndexLaunch> prepare(Launch launch, List<TestItem> testItems,
      AnalyzerConfig analyzerConfig);

  Optional<IndexLaunch> prepare(Long id, AnalyzerConfig analyzerConfig);

  List<IndexLaunch> prepare(List<Long> ids, AnalyzerConfig analyzerConfig);

  List<IndexLaunch> prepare(AnalyzerConfig analyzerConfig, List<TestItem> testItems);

}
