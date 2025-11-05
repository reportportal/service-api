package com.epam.ta.reportportal.core.events.handler.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.logtype.DefaultLogTypeProvider;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import java.time.Instant;
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
    Activity activity = createProjectCreatedActivity(projectId, "Test Project");
    ProjectLogType warn = new ProjectLogType();
    warn.setLevel(30000);
    warn.setName("warn");
    ProjectLogType info = new ProjectLogType();
    info.setLevel(20000);
    info.setName("info");

    when(defaultLogTypeProvider.provideDefaultLogTypes(projectId)).thenReturn(List.of(warn, info));

    // when
    projectLogTypesInitializer.onProjectCreated(activity);

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
    Activity activityWithNullId = createProjectCreatedActivity(null, "Test Project");

    // when
    projectLogTypesInitializer.onProjectCreated(activityWithNullId);

    // then
    verifyNoInteractions(defaultLogTypeProvider, logTypeRepository);
  }

  @Test
  void onProjectCreatedShouldRollbackAndLogErrorOnFailure() {
    // given
    Long projectId = 1L;
    Activity activity = createProjectCreatedActivity(projectId, "Test Project");

    when(defaultLogTypeProvider.provideDefaultLogTypes(projectId))
        .thenThrow(new RuntimeException("Database error"));

    // when + then
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> projectLogTypesInitializer.onProjectCreated(activity));
    assertEquals("Database error", exception.getMessage());

    verify(defaultLogTypeProvider).provideDefaultLogTypes(projectId);
    verifyNoInteractions(logTypeRepository);
  }

  private Activity createProjectCreatedActivity(Long projectId, String projectName) {
    Activity activity = new Activity();
    activity.setAction(EventAction.CREATE);
    activity.setEventName("create_project");
    activity.setPriority(EventPriority.MEDIUM);
    activity.setObjectId(projectId);
    activity.setObjectName(projectName);
    activity.setObjectType(EventObject.PROJECT);
    activity.setProjectId(projectId);
    activity.setSubjectId(10L);
    activity.setSubjectName("testUser");
    activity.setSubjectType(EventSubject.USER);
    activity.setCreatedAt(Instant.now());
    return activity;
  }
}
