package com.epam.ta.reportportal.core.events.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.event.LaunchStartUniqueErrorAnalysisEvent;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.project.config.ProjectConfigProvider;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class StartLaunchUniqueErrorAnalysisEventListenerTest {

  private final ProjectConfigProvider projectConfigProvider = mock(ProjectConfigProvider.class);
  private final UniqueErrorAnalysisStarter starter = mock(UniqueErrorAnalysisStarter.class);

  private final StartLaunchUniqueErrorAnalysisEventListener listener = new StartLaunchUniqueErrorAnalysisEventListener(
      projectConfigProvider,
      starter
  );

  @Test
  void shouldStart() {
    final Map<String, String> projectConfig = Collections.emptyMap();
    when(projectConfigProvider.provide(anyLong())).thenReturn(projectConfig);

    final LaunchStartUniqueErrorAnalysisEvent event = new LaunchStartUniqueErrorAnalysisEvent(1L,
        1L);
    listener.onApplicationEvent(event);

    final ArgumentCaptor<ClusterEntityContext> contextArgumentCaptor = ArgumentCaptor.forClass(
        ClusterEntityContext.class);
    verify(starter, times(1)).start(contextArgumentCaptor.capture(), eq(projectConfig));

    final ClusterEntityContext entityContext = contextArgumentCaptor.getValue();

    assertEquals(event.getSource(), entityContext.getLaunchId());
    assertEquals(event.getProjectId(), entityContext.getProjectId());
  }

}