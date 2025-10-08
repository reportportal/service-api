package com.epam.ta.reportportal.core.events.handler.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.events.activity.ProjectCreatedEvent;
import com.epam.ta.reportportal.core.logtype.DefaultLogTypeProvider;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectLogTypesInitializerTest {

  @Mock
  private DefaultLogTypeProvider defaultLogTypeProvider;

  @Mock
  private LogTypeRepository logTypeRepository;

  @InjectMocks
  private ProjectLogTypesInitializer projectLogTypesInitializer;

  @Captor
  private ArgumentCaptor<List<ProjectLogType>> logTypeCaptor;

  @Test
  void onProjectCreatedShouldInitializeLogTypesWhenEventIsValid() {
    // given
    Long projectId = 1L;
    ProjectCreatedEvent event = new ProjectCreatedEvent(10L, "testUser", projectId, "Test Project");
    ProjectLogType warn = new ProjectLogType();
    warn.setLevel(30000);
    warn.setName("warn");
    ProjectLogType info = new ProjectLogType();
    info.setLevel(20000);
    info.setName("info");

    when(defaultLogTypeProvider.provideDefaultLogTypes(projectId)).thenReturn(List.of(warn, info));

    // when
    projectLogTypesInitializer.onProjectCreated(event);

    // then
    verify(defaultLogTypeProvider).provideDefaultLogTypes(projectId);
    verify(logTypeRepository).saveAll(logTypeCaptor.capture());

    List<ProjectLogType> capturedLogTypes = logTypeCaptor.getValue();
    assertEquals(2, capturedLogTypes.size());
    assertEquals("warn", capturedLogTypes.get(0).getName());
    assertEquals(30000, capturedLogTypes.get(0).getLevel());
    assertEquals("info", capturedLogTypes.get(1).getName());
    assertEquals(20000, capturedLogTypes.get(1).getLevel());
  }

  @Test
  void onProjectCreated_ShouldLogWarning_WhenEventIsNullOrProjectIdIsNull() {
    // given
    ProjectCreatedEvent eventWithNullId = new ProjectCreatedEvent(10L, "testUser", null,
        "Test Project");

    // when
    projectLogTypesInitializer.onProjectCreated(eventWithNullId);

    // then
    verifyNoInteractions(defaultLogTypeProvider, logTypeRepository);
  }

  @Test
  void onProjectCreatedShouldRollbackAndLogErrorOnFailure() {
    // given
    Long projectId = 1L;
    ProjectCreatedEvent event = new ProjectCreatedEvent(10L, "testUser", projectId, "Test Project");

    when(defaultLogTypeProvider.provideDefaultLogTypes(projectId))
        .thenThrow(new RuntimeException("Database error"));

    // when + then
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> projectLogTypesInitializer.onProjectCreated(event));
    assertEquals("Database error", exception.getMessage());

    verify(defaultLogTypeProvider).provideDefaultLogTypes(projectId);
    verifyNoInteractions(logTypeRepository);
  }
}
