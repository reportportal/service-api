/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data.resolver.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.ClusterDataProvider;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ClusterDataProviderEvaluatorTest {

  private final Predicate<GenerateClustersConfig> predicate = (Predicate<GenerateClustersConfig>) mock(
      Predicate.class);
  private final ClusterDataProvider clusterDataProvider = mock(ClusterDataProvider.class);

  private final ClusterDataProviderEvaluator evaluator = new ClusterDataProviderEvaluator(predicate,
      clusterDataProvider);

  @Test
  void shouldReturnTrueWhenPredicateIsTrue() {
    when(predicate.test(any(GenerateClustersConfig.class))).thenReturn(true);
    assertTrue(evaluator.supports(new GenerateClustersConfig()));
  }

  @Test
  void shouldReturnFalseWhenPredicateIsFalse() {
    when(predicate.test(any(GenerateClustersConfig.class))).thenReturn(false);
    assertFalse(evaluator.supports(new GenerateClustersConfig()));
  }

  @Test
  void providerShouldBeEqual() {
    assertEquals(clusterDataProvider, evaluator.getProvider());
  }

}