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

package com.epam.ta.reportportal.core.launch.cluster.pipeline;

import static com.epam.ta.reportportal.core.launch.cluster.utils.ConfigProvider.getConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.launch.cluster.CreateClusterHandler;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.ClusterDataProvider;
import com.epam.ta.reportportal.core.launch.cluster.pipeline.data.resolver.ClusterDataProviderResolver;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class SaveClusterDataPartProviderTest {

  private final ClusterDataProvider dataProvider = mock(ClusterDataProvider.class);
  private final ClusterDataProviderResolver resolver = mock(ClusterDataProviderResolver.class);
  private final CreateClusterHandler createClusterHandler = mock(CreateClusterHandler.class);

  private final SaveClusterDataPartProvider provider = new SaveClusterDataPartProvider(resolver,
      createClusterHandler);

  @Test
  void shouldSaveWhenProviderAndDataExists() {
    final GenerateClustersConfig config = getConfig(false);

    final ClusterData clusterData = new ClusterData();
    when(dataProvider.provide(config)).thenReturn(Optional.of(clusterData));
    when(resolver.resolve(config)).thenReturn(Optional.of(dataProvider));

    final PipelinePart pipelinePart = provider.provide(config);
    pipelinePart.handle();

    verify(createClusterHandler, times(1)).create(clusterData);
  }

  @Test
  void shouldSaveWhenProviderNotExists() {
    final GenerateClustersConfig config = getConfig(false);

    final ClusterData clusterData = new ClusterData();
    when(resolver.resolve(config)).thenReturn(Optional.empty());

    final PipelinePart pipelinePart = provider.provide(config);
    pipelinePart.handle();

    verify(createClusterHandler, times(0)).create(clusterData);
  }

  @Test
  void shouldNotSaveWhenProviderExistsAndNoDataExists() {
    final GenerateClustersConfig config = getConfig(false);

    final ClusterData clusterData = new ClusterData();
    when(dataProvider.provide(config)).thenReturn(Optional.of(clusterData));
    when(resolver.resolve(config)).thenReturn(Optional.empty());

    final PipelinePart pipelinePart = provider.provide(config);
    pipelinePart.handle();

    verify(createClusterHandler, times(0)).create(clusterData);
  }

}