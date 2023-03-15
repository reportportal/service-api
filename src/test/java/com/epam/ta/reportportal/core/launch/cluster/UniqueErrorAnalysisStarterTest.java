package com.epam.ta.reportportal.core.launch.cluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class UniqueErrorAnalysisStarterTest {

  private final ClusterGenerator clusterGenerator = mock(ClusterGenerator.class);

  private final UniqueErrorAnalysisStarter starter = new UniqueErrorAnalysisStarter(
      clusterGenerator);

  @Test
  void shouldGenerateNotForUpdateWhenNoItemIds() {

    final ClusterEntityContext entityContext = ClusterEntityContext.of(1L, 1L);

    starter.start(entityContext, new HashMap<>());

    final ArgumentCaptor<GenerateClustersConfig> configArgumentCaptor = ArgumentCaptor.forClass(
        GenerateClustersConfig.class);
    verify(clusterGenerator, times(1)).generate(configArgumentCaptor.capture());

    final GenerateClustersConfig generateClustersConfig = configArgumentCaptor.getValue();

    assertFalse(generateClustersConfig.isForUpdate());
    assertEquals(entityContext.getLaunchId(),
        generateClustersConfig.getEntityContext().getLaunchId());
    assertEquals(entityContext.getProjectId(),
        generateClustersConfig.getEntityContext().getProjectId());
    assertEquals(entityContext.getItemIds(),
        generateClustersConfig.getEntityContext().getItemIds());
  }

  @Test
  void shouldGenerateForUpdateWhenItemIdsExist() {
    final ClusterEntityContext entityContext = ClusterEntityContext.of(1L, 1L, List.of(1L, 2L));

    starter.start(entityContext, new HashMap<>());

    final ArgumentCaptor<GenerateClustersConfig> configArgumentCaptor = ArgumentCaptor.forClass(
        GenerateClustersConfig.class);
    verify(clusterGenerator, times(1)).generate(configArgumentCaptor.capture());

    final GenerateClustersConfig generateClustersConfig = configArgumentCaptor.getValue();

    assertTrue(generateClustersConfig.isForUpdate());
    assertEquals(entityContext.getLaunchId(),
        generateClustersConfig.getEntityContext().getLaunchId());
    assertEquals(entityContext.getProjectId(),
        generateClustersConfig.getEntityContext().getProjectId());
    assertEquals(entityContext.getItemIds(),
        generateClustersConfig.getEntityContext().getItemIds());
  }

}